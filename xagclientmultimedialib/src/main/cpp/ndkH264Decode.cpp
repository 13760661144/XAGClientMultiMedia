

#ifdef __cplusplus
extern "C" {
#endif
#include"common.h"
#include"opengles_display.h"
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <libavutil/imgutils.h>
#include <libavdevice/avdevice.h>
#include <libavcodec/avcodec.h>

#define BUF_SIZE 1024 * 64

static void ffmpeg_log_handler(
        void *ptr, int lev, const char *fmt, va_list args) {
    int prio;

    switch (lev) {
        case AV_LOG_DEBUG:
            prio = ANDROID_LOG_DEBUG;
            break;
        case AV_LOG_INFO:
            prio = ANDROID_LOG_INFO;
            break;
        case AV_LOG_WARNING:
            prio = ANDROID_LOG_WARN;
            break;
        case AV_LOG_ERROR:
            prio = ANDROID_LOG_ERROR;
            break;
        case AV_LOG_FATAL:
            prio = ANDROID_LOG_FATAL;
            break;
        default:
            prio = ANDROID_LOG_DEFAULT;
            break;
    }

    __android_log_vprint(prio, LOG_TAG, fmt, args);
}

static const char *resultDescription(int result) {
    switch (result) {
        case 0:
            return "No errors.";

        case -1:
            return "Error -1";
    }
    return "Could not define result\n";
}

typedef struct _DecData {
    struct SwsContext *sws_ctx;
    AVCodecContext av_context;
    unsigned int packet_num;
    uint8_t *bitstream;
    int bitstream_size;
    uint64_t last_error_reported_time;
    bool_t first_image_decoded;
    bool_t isStoped;
} DecData;

static void ffmpeg_init() {
    static bool_t done = FALSE;
    av_log_set_callback(ffmpeg_log_handler);
    if (!done) {
#ifdef FF_API_AVCODEC_INIT
        avcodec_init();
#endif
        avcodec_register_all();
        done = TRUE;
    }
}
typedef struct AndroidDisplay {
    jobject android_video_window;
    jmethodID set_opengles_display_id;
    jmethodID request_render_id;
    jmethodID j_sync_stream;
    jmethodID j_push_buffer;
    struct opengles_display *ogl;
} AndroidDisplay;

AndroidDisplay *ad = NULL;
JavaVM *g_jvmInstance = 0;
pthread_key_t jnienv_key;
JNIEnv *get_jni_env(void) {
    JNIEnv *env = NULL;
    if (g_jvmInstance == NULL) {
        LOGE("NO jvm");
    } else {
        env = (JNIEnv *) pthread_getspecific(jnienv_key);
        if (env == NULL) {
            LOGE("AttachCurrentThread()");
            if (g_jvmInstance->AttachCurrentThread(&env, NULL) != 0) {
                LOGE("AttachCurrentThread() failed !");
                return NULL;
            }
            LOGE("AttachCurrentThread() success !");
            pthread_setspecific(jnienv_key, env);
        }
    }
    return env;
}
void _android_key_cleanup(void *data) {
    LOGV("_android_key_cleanup");
    JNIEnv *env = (JNIEnv *) pthread_getspecific(jnienv_key);
    if (env != NULL) {
        LOGV("Thread end, detaching jvm from current thread g_jvmInstance = %p", g_jvmInstance);
        g_jvmInstance->DetachCurrentThread();
        LOGV("Thread end, begin pthread_setspecific &jnienv_key = %p", &jnienv_key);
        pthread_setspecific(jnienv_key, NULL);
        LOGV("Thread end, end pthread_setspecific &jnienv_key = %p", &jnienv_key);
    }
}

jint JNI_OnLoad(JavaVM *jvm, void *reserved) {
    LOGV("JNI_OnLoad");
    g_jvmInstance = jvm;
    LOGV("g_jvmInstance = %p", g_jvmInstance);
    pthread_key_create(&jnienv_key, _android_key_cleanup);
    LOGV("&jnienv_key = %p", &jnienv_key);
    return JNI_VERSION_1_4;
}

void JNI_OnUnload(JavaVM *jvm, void *reserved) {
    JNIEnv *env;
    LOGV("JNI_OnUnload");
    g_jvmInstance = 0;

    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGV("Get the current java vm enviroment fail!!\n");
        return;
    } else {
        LOGV("Get the current java vm enviroment done!!\n");
    }
}
void unInitAndroidDisplay() {
    JNIEnv *jenv = get_jni_env();
    if (ad->ogl) {
        /* clear native ptr, to prevent rendering to occur now that ptr is invalid */
        if (ad->android_video_window)
            jenv->CallVoidMethod(ad->android_video_window, ad->set_opengles_display_id, 0);
        ogl_display_uninit(ad->ogl, TRUE);
        ogl_display_free(ad->ogl);
        ad->ogl = NULL;
    }
    if (ad->android_video_window) jenv->DeleteGlobalRef(ad->android_video_window);

    av_free(ad);
    ad = NULL;
}
static void dec_open(DecData *d) {
    AVCodec *codec;
    int error;
    codec = avcodec_find_decoder(AV_CODEC_ID_H264);
//	codec=avcodec_find_decoder(CODEC_ID_H264);
    if (codec == NULL) LOGE("Could not find H264 decoder in ffmpeg.");
    else {
        LOGV("Found H264 CODEC");
    }
    avcodec_get_context_defaults3(&d->av_context, NULL);
    //d->av_context.flags2 |= CODEC_FLAG2_CHUNKS;
    error = avcodec_open2(&d->av_context, codec, NULL);
    if (error != 0) {
        LOGE("avcodec_open() failed.");
    } else {
        LOGV("OPENED H264 CODEC");
    }
}

DecData *d = NULL;
static void dec_init() {
    d = (DecData *) av_mallocz(sizeof(DecData));
    ffmpeg_init();
    d->sws_ctx = NULL;
    d->packet_num = 0;
    dec_open(d);
    d->bitstream_size = 65535;
    d->bitstream = (uint8_t *) av_mallocz(d->bitstream_size);
    d->last_error_reported_time = 0;
    d->isStoped = false;
}
static void dec_uninit() {
    if (d->sws_ctx != NULL) {
        sws_freeContext(d->sws_ctx);
        d->sws_ctx = NULL;
    }
    avcodec_close(&d->av_context);
    av_free(d->bitstream);
    av_free(d);
    d = NULL;
}

int verifyFile(const char *fileFrom) {
    LOGV("verifyFile in");
    FILE *inputBufferFile = fopen(fileFrom, "rb");
    if (inputBufferFile == NULL) {
        LOGV("Failed to open the file %s\n", fileFrom);
        return -1;
    }
    fclose(inputBufferFile);

    LOGV("verifyFile out");
    return 0;
}

int readBufferFromH264InputFile(FILE *file, uint8_t *buff, int maxBuffSize, long *timeStampLfile) {
    unsigned int readOffset = 0;
    int bytes_read = 0;
    unsigned int code = 0;

    do {
        //Start codes are always byte aligned.
        bytes_read = fread(buff + readOffset, 1, 1, file);
        if (bytes_read == 0) {
            LOGE("QCOMOMXINTERFACE - Failed to read frame or it might be EOF\n \n");
            return 0;
        }
        code <<= 8;
        code |= (0x000000FF & buff[readOffset]);

        //VOP start code comparision //util read the second frame start code
        if ((readOffset > 3) && (H264_START_CODE == code)) {
            //Seek backwards by 4
            fseek(file, -4, SEEK_CUR);
            readOffset -= 3;
            break;
        }
        readOffset++;

        if ((int) readOffset >= maxBuffSize - 1) {
            if ((int) readOffset > maxBuffSize - 1) {
                int diff = readOffset - maxBuffSize;
                readOffset -= diff;
                fseek(file, -diff, SEEK_CUR);
            }
            break;
        }

    } while (1);

    *timeStampLfile += 1000000 / 10;
    return readOffset;
}

static void yuv_buf_init(YuvBuf *buf, int w, int h, uint8_t *ptr) {
    int ysize, usize;
    ysize = w * h;
    usize = ysize / 4;
    buf->w = w;
    buf->h = h;
    buf->planes[0] = ptr;
    buf->planes[1] = buf->planes[0] + ysize;
    buf->planes[2] = buf->planes[1] + usize;
    buf->planes[3] = 0;
    buf->strides[0] = w;
    buf->strides[1] = w / 2;
    buf->strides[2] = buf->strides[1];
    buf->strides[3] = 0;
}

int decode_file(const char *fileFrom) {
    int errorCode = verifyFile(fileFrom);
    LOGV("decode_file errorCode = %d", errorCode);
    if (errorCode != 0) {
        return errorCode;
    }
    FILE *fFrom = fopen(fileFrom, "rb");

    //char buffer[3350000];
    //	int maxBuffSize = 3350000;
    long timeStampLfile = 0;
    int totalSize = 0;

    int size = 0;
    AVFrame *orig = av_frame_alloc();
    uint8_t *p, *end;
    LOGV("decode_file in while ");
    do {
        size = readBufferFromH264InputFile(fFrom, d->bitstream, d->bitstream_size, &timeStampLfile);
        LOGV("readBufferFromH264InputFile size = %d", size);
        int result = 0;
        p = d->bitstream;
        end = d->bitstream + size;
        while (end - p > 0 && !d->isStoped) {
            int len;
            int got_picture = 0;
            AVPacket pkt;
            //avcodec_get_frame_defaults(orig);
            av_init_packet(&pkt);
            pkt.data = p;
            pkt.size = end - p;
            LOGE("package size is %d", pkt.size);
            len = avcodec_decode_video2(&d->av_context, orig, &got_picture, &pkt);
            LOGV("decoded data len = %d    got_picture = %d", len, got_picture);
            LOGV("av_context.width = %d    av_context.height = %d  av_context.pix_fmt = %d",
                 d->av_context.width, d->av_context.height, d->av_context.pix_fmt);
            if (len <= 0) {
                LOGE("ms_AVdecoder_process: error %i.", len);
                break;
            }
            if (got_picture) {
                LOGV("one Frame Decoded");
                LOGV("Frame width = %d,height = %d,keyFrame = %d format = %d", orig->width,
                     orig->height, orig->key_frame, orig->format);

                if (d->sws_ctx == NULL) {
                    d->sws_ctx = sws_getContext(d->av_context.width, d->av_context.height,
                                                d->av_context.pix_fmt,
//							d->av_context.width,d->av_context.height,PIX_FMT_YUV420P,SWS_FAST_BILINEAR,
                                                d->av_context.width, d->av_context.height,
                                                AV_PIX_FMT_YUV420P, SWS_FAST_BILINEAR,
                                                NULL, NULL, NULL);
                }
                YuvBuf *yuvbuff = (YuvBuf *) av_mallocz(sizeof(YuvBuf));
                yuvbuff->w = orig->width;
                yuvbuff->h = orig->height;
                uint8_t *ptr = (uint8_t *) av_mallocz((yuvbuff->w * yuvbuff->h * 3) / 2);
                yuv_buf_init(yuvbuff, yuvbuff->w, yuvbuff->h, ptr);
#if LIBSWSCALE_VERSION_INT >= AV_VERSION_INT(0, 9, 0)
                if (sws_scale(d->sws_ctx, (const uint8_t *const *) orig->data, orig->linesize, 0,
                              d->av_context.height, yuvbuff->planes, yuvbuff->strides) < 0) {
#else
                    if (sws_scale(d->sws_ctx,(uint8_t **)orig->data,orig->linesize, 0,
                            d->av_context.height, yuvbuff->planes, yuvbuff->strides)<0){
#endif
                    LOGE("error in sws_scale().");
                }

                ogl_display_set_yuv_to_display(ad->ogl, yuvbuff);
                LOGV("ad->android_video_window = %p", ad->android_video_window);
                JNIEnv *env = get_jni_env();
                env->CallVoidMethod(ad->android_video_window, ad->request_render_id);
            }
            p += len;
        }

    } while (size > 0 && !d->isStoped);
    av_frame_free(&orig);
    totalSize += size;
    fclose(fFrom);

    return errorCode;
}

int av_read_data(void *opaque, uint8_t *buf, int buf_size) {
    int b_len = 0;
    int len = buf_size;
    int read_len = 0;
    JNIEnv *jenv = get_jni_env();
    jbyteArray byarray = jenv->NewByteArray(buf_size);
    jbyte *dat = NULL;

    while (len > 0) {
        read_len = jenv->CallIntMethod(ad->android_video_window, ad->j_sync_stream, byarray, len);
        if (read_len > 0) {
            dat = jenv->GetByteArrayElements(byarray, NULL);
            if (dat == NULL)
                LOGE("Failed to GetByteArrayElements !");

            memcpy(buf + b_len, dat, read_len);
            b_len += read_len;
            len -= read_len;
            if (dat != NULL)
                jenv->ReleaseByteArrayElements(byarray, dat, 0);
        }
    }

    jenv->DeleteLocalRef(byarray);
    return b_len;
}

pthread_t _threadDecode;
AVIOContext *pb = NULL;
AVInputFormat *piFmt = NULL;
AVFormatContext *pFormatCtx;
AVCodecContext *pCodecCtx;
AVCodec *pCodec;
AVCodecID avCodecID;

static int isRunning = 0;


void *DecodeThread(void *obj) {
    AVPacket *packet;

    packet = (AVPacket *) av_malloc(sizeof(AVPacket));

    JNIEnv *jenv = get_jni_env();

    while (av_read_frame(pFormatCtx, packet) >= 0) {
        if (packet->size > 0) {
            jobject jbyteBuffer = jenv->NewDirectByteBuffer(packet->data, packet->size);
            jenv->CallVoidMethod(ad->android_video_window, ad->j_push_buffer, jbyteBuffer);
            jenv->DeleteLocalRef(jbyteBuffer);
        }
        av_free_packet(packet);
    }

    av_free_packet(packet);

    avcodec_close(pCodecCtx);
    avformat_close_input(&pFormatCtx);

    if (ad->android_video_window)
        jenv->DeleteGlobalRef(ad->android_video_window);

    av_free(ad);
    ad = NULL;

    return ((void *) 0);
}

uint8_t *buf = NULL;
extern "C"
JNIEXPORT void JNICALL
Java_cn_xag_xagclientmultimefialib_jniutils_FFmpegDecodH264_startVideoStream(JNIEnv *env,
                                                                             jobject instance,
                                                                             jobject id,
                                                                             jobject surface) {

    //    com.example.ning.arlinkplayerdemo
    LOGE("Start Video Stream Analysis");
    int i, videoindex;
    int retry_time = 0;

    if (ad == NULL)
        ad = (AndroidDisplay *) av_mallocz(sizeof(AndroidDisplay));
    jclass wc = env->FindClass("cn/xag/xagclientmultimefialib/jniutils/FFmpegDecodH264");
    if (wc == 0) {
        LOGE("Could not find com.example.ning.arlinkplayerdemo.MainActivity class !");
    }

    ad->j_sync_stream = env->GetMethodID(wc, "syncStream", "([BI)I");
    if (ad->j_sync_stream == 0)
        LOGE("Could not find 'syncStream' method\n");

    ad->j_push_buffer = env->GetMethodID(wc, "pushFrameBuffer", "(Ljava/nio/ByteBuffer;)V");
    if (ad->j_push_buffer == 0)
        LOGE("Could not find 'j_push_buffer' method\n");

    if (id) {
        ad->android_video_window = (env)->NewGlobalRef(id);
    } else
        ad->android_video_window = NULL;

    env->DeleteLocalRef(wc);


    buf = (uint8_t *) av_mallocz(BUF_SIZE);

    av_log_set_callback(ffmpeg_log_handler);
    avcodec_register_all();
    av_register_all();
    pb = avio_alloc_context(buf, BUF_SIZE, 0, NULL, av_read_data, NULL, NULL);
    if (pb == NULL)
        LOGE("avio_alloc_context失败了!!!");
    bool inital_ok = false;
    while (!inital_ok) {
        piFmt = NULL;
        if (av_probe_input_buffer(pb, &piFmt, NULL, NULL, 0,1024 * 64) < 0) {
            LOGE("av_probe_input_buffer 失败!\n");
            continue;
        } else {
            LOGE("av_probe_input_buffer 成功!\n");
        }

        pFormatCtx = avformat_alloc_context();
        pFormatCtx->pb = pb;

        while (avformat_open_input(&pFormatCtx, "", piFmt, NULL) != 0) {
            LOGE("无法打开输入流\n");
            continue;
        }

//			pFormatCtx->probesize = 512 * 1024;
//			pFormatCtx->max_analyze_duration = AV_TIME_BASE;
//			pFormatCtx->flags |= AVFMT_FLAG_NOBUFFER;
//
//			while(avformat_find_stream_info(pFormatCtx, NULL) < 0){
//				LOGE("Couldn't find stream information.\n");
//				if (++retry_time >= 20)
//					break;
//			}
//

        videoindex = -1;
        for (i = 0; i < pFormatCtx->nb_streams; i++)
            if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
                videoindex = i;
                break;
            }
        if (videoindex == -1) {
            LOGE("无法找到视频流.\n");
            continue;
        }
        pCodecCtx = pFormatCtx->streams[videoindex]->codec;

        if (pCodecCtx->width == 0 || pCodecCtx->height == 0) {
            pCodecCtx->width = 1280;
            pCodecCtx->height = 720;
        }
        if (pCodecCtx != NULL)
            LOGE("pCodecCtx is not NULL!\n");
        pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
        if (pCodec == NULL) {
            LOGE("Couldn't find Codec.\n");
            continue;
        }

        LOGE("Stream Size is %d x %d \n", pCodecCtx->width, pCodecCtx->height);
        if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
            LOGE("Couldn't open codec.\n");
            continue;
        }
        LOGE("Now Stream Size is %d x %d \n", pCodecCtx->width, pCodecCtx->height);
        inital_ok = true;
        // 获取native window
        ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);

        // 获取视频宽高
        int videoWidth = pCodecCtx->width;
        int videoHeight = pCodecCtx->height;
        // 设置native window的buffer大小,可自动拉伸
        ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight,
                                         WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_Buffer windowBuffer;
        // Allocate video frame
        AVFrame *pFrame = av_frame_alloc();

        // 用于渲染
        AVFrame *pFrameRGBA = av_frame_alloc();
        if (pFrameRGBA == NULL || pFrame == NULL) {
            LOGE("Could not allocate video frame.");
            return;
        }
        // Determine required buffer size and allocate buffer
        // buffer中数据就是用于渲染的,且格式为RGBA
        int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height,
                                                1);
        uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
        av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                             pCodecCtx->width, pCodecCtx->height, 1);

        // 由于解码出来的帧格式不是RGBA的,在渲染之前
        // 需要进行格式转换
        struct SwsContext *sws_ctx = sws_getContext(pCodecCtx->width,
                                                    pCodecCtx->height,
                                                    AV_PIX_FMT_YUV420P,
                                                    pCodecCtx->width,
                                                    pCodecCtx->height,
                                                    AV_PIX_FMT_RGBA,
                                                    SWS_BILINEAR,
                                                    NULL,
                                                    NULL,
                                                    NULL);

        int frameFinished;
        AVPacket packet;
        while (av_read_frame(pFormatCtx, &packet) >= 0) {
            // Is this a packet from the video stream?
            if (packet.stream_index == videoindex) {

                // Decode video frame
                avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);

                // 并不是decode一次就可解码出一帧
                if (frameFinished) {

                    // lock native window buffer
                    ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

                    // 格式转换
                    sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
                              pFrame->linesize, 0, pCodecCtx->height,
                              pFrameRGBA->data, pFrameRGBA->linesize);

                    // 获取stride
                    uint8_t *dst = (uint8_t *) windowBuffer.bits;
                    int dstStride = windowBuffer.stride * 4;
                    uint8_t *src = (pFrameRGBA->data[0]);
                    int srcStride = pFrameRGBA->linesize[0];

                    // 由于window的stride和帧的stride不同,因此需要逐行复制
                    int h;
                    for (h = 0; h < videoHeight; h++) {
                        memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                    }

                    ANativeWindow_unlockAndPost(nativeWindow);
                }
            }
            usleep(2500);
            av_packet_unref(&packet);
        }

        av_free(buffer);
        av_free(pFrameRGBA);

        // Free the YUV frame
        av_free(pFrame);

        // Close the codecs
        avcodec_close(pCodecCtx);

        // Close the video file
        avformat_close_input(&pFormatCtx);
    }
  /*  isRunning = 1;
    if (0 != pthread_create(&_threadDecode, NULL, DecodeThread, NULL)) {
        LOGE("Could not create decode thread !!!");
    }*/

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_ning_arlinkplayerdemo_MainActivity_stopVideo(JNIEnv *env, jobject instance) {

    // TODO
    isRunning = 0;
}

//void Java_com_example_arlinkplayer_ArtosynPlayer_stopVideo(JNIEnv *env, jclass cls) {
//    isRunning = 0;
//}

jstring Java_com_example_arlinkplayer_ActivityH264Decoder_decodeFile
        (JNIEnv *env, jclass cls, jstring infile, jstring outfile, jint width, jint height) {
    const char *inputFileName = env->GetStringUTFChars(infile, 0);
    const char *outputFileName = env->GetStringUTFChars(outfile, 0);
    dec_init();
    __android_log_print(ANDROID_LOG_ERROR, "LOG TAG", "DECODING FROM %s TO %s\n", inputFileName,
                        outputFileName);
    LOGV("before decode_file");
    int ret = decode_file(inputFileName);
    LOGV("decode_file ret = %d", ret);
    const char *result = resultDescription(ret);
    dec_uninit();
    __android_log_print(ANDROID_LOG_ERROR, "LOG TAG", "RESULT: %s\n", result);
    return env->NewStringUTF(result);
}


static AVPacket *pPacket;
static AVFrame *pAvFrame, *pFrameBGR;
static AVCodecContext *pCodecCtx1;
struct SwsContext *pImgConvertCtx;
static AVFormatContext *pFormatCtx1;
ANativeWindow* nativeWindow;
AVDictionary* options = NULL;
ANativeWindow_Buffer windowBuffer;
uint8_t *v_out_buffer;
bool stop;
extern "C"
JNIEXPORT jint JNICALL
Java_cn_xag_xagclientmultimefialib_jniutils_FFmpegDecodH264_startRtpsVideo(JNIEnv *env,
                                                                           jobject instance,
                                                                           jstring url_,
                                                                           jobject surface) {
    stop = false;
    // 初始化
#if LIBAVCODEC_VERSION_INT < AV_VERSION_INT(55, 28, 1)
#define av_frame_alloc  avcodec_alloc_frame
#endif

    pAvFrame = av_frame_alloc();
    pFrameBGR = av_frame_alloc();

    char input_str[500]={0};
    sprintf(input_str, "%s", env->GetStringUTFChars(url_, NULL));
    nativeWindow = ANativeWindow_fromSurface(env, surface);
    if (0 == nativeWindow){
        return -1;
    }

    //初始化
    avcodec_register_all();
    av_register_all();         //注册库中所有可用的文件格式和编码器
    avformat_network_init();

    //av_dict_set(&options, "buffer_size", "1024000", 0);
  //  av_dict_set(&options, "rtsp_transport", "tcp", 0);  //以udp方式打开，如果以tcp方式打开将udp替换为tcp
  //  av_dict_set(&options, "stimeout", "2000000", 0);
    //av_dict_set(&options, "max_delay", "500000", 0);

    pFormatCtx1 = avformat_alloc_context();
    if (avformat_open_input(&pFormatCtx1, input_str, NULL, NULL) < 0)
        return 1;
    avformat_find_stream_info(pFormatCtx1, NULL);

    int videoIndex = -1;
    for (unsigned int i = 0; i < pFormatCtx1->nb_streams; i++) //遍历各个流，找到第一个视频流,并记录该流的编码信息
    {
        if (pFormatCtx1->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;                                     //这里获取到的videoindex的结果为1.
            break;
        }
    }
    pCodecCtx1 = pFormatCtx1->streams[videoIndex]->codec;
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx1->codec_id);
    avcodec_open2(pCodecCtx1, pCodec, NULL);

    int width = pCodecCtx1->width;
    int height = pCodecCtx1->height;

    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 1);
    v_out_buffer = (uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
    av_image_fill_arrays(pFrameBGR->data, pFrameBGR->linesize, v_out_buffer, AV_PIX_FMT_RGBA, width, height, 1);
    pImgConvertCtx = sws_getContext(
            pCodecCtx1->width,             //原始宽度
            pCodecCtx1->height,            //原始高度
            pCodecCtx1->pix_fmt,           //原始格式
            pCodecCtx1->width,             //目标宽度
            pCodecCtx1->height,            //目标高度
            AV_PIX_FMT_RGBA,               //目标格式
            SWS_BICUBIC,                    //选择哪种方式来进行尺寸的改变,关于这个参数,可以参考:http://www.cnblogs.com/mmix2009/p/3585524.html
            NULL,
            NULL,
            NULL);
    if (0 > ANativeWindow_setBuffersGeometry(nativeWindow,width,height,WINDOW_FORMAT_RGBA_8888)){
        ANativeWindow_release(nativeWindow);
        return -1;
    }

//    int avPicSize = avpicture_get_size(AV_PIX_FMT_BGR24, pCodecCtx->width, pCodecCtx->height);
//    uint8_t *pPicBuffer = (uint8_t *) av_malloc(avPicSize);
//    avpicture_fill((AVPicture *) pFrameBGR, pPicBuffer, AV_PIX_FMT_BGR24, pCodecCtx->width,
//                   pCodecCtx->height);

    pPacket = (AVPacket*)av_malloc(sizeof(AVPacket));
    // 读取数据包
    int count = 0;
    while (!stop) {
        if (av_read_frame(pFormatCtx1, pPacket) >= 0) {
            if ((pPacket)->stream_index != videoIndex) {
                // 包不对，不解码
                continue;
            }
            if ((pPacket)->nIsLostPackets == 1)
            {
                // 丢包，不解码
                LOGV("nIsLostPackets");
                continue;
            }

            //解码
            int gotPicCount = 0;

            int code = avcodec_decode_video2(pCodecCtx1, pAvFrame, &gotPicCount, pPacket);
            if (gotPicCount != 0) {
                count++;
                sws_scale(
                        pImgConvertCtx,
                        (const uint8_t *const *) pAvFrame->data,
                        pAvFrame->linesize,
                        0,
                        pCodecCtx1->height,
                        pFrameBGR->data,
                        pFrameBGR->linesize);
                //cv::Mat tmpFrame(cv::Size(pCodecCtx->width, pCodecCtx->height), CV_8UC3);//转换为openCv的mat类型

                if (ANativeWindow_lock(nativeWindow, &windowBuffer, NULL) < 0) {

                } else {
                    uint8_t *dst = (uint8_t *) windowBuffer.bits;
                    for (int h = 0; h < height; h++) {
                        memcpy(dst + h * windowBuffer.stride * 4,
                               v_out_buffer + h * pFrameBGR->linesize[0],
                               pFrameBGR->linesize[0]);
                    }
                    ANativeWindow_unlockAndPost(nativeWindow);
                }
            }
        }
    //    usleep(3000);
        av_packet_unref(pPacket);
    }
    sws_freeContext(pImgConvertCtx);
    av_free(pPacket);
    av_free(pFrameBGR);
    avcodec_close(pCodecCtx1);
    avformat_close_input(&pFormatCtx1);
    return 1;
}
#ifdef __cplusplus
}
#endif
