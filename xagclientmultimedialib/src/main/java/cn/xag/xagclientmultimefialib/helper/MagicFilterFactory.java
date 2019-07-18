package cn.xag.xagclientmultimefialib.helper;

import android.content.Context;

import cn.xag.xagclientmultimefialib.fliter.GPUImageFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicAntiqueFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicBrannanFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicCoolFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicFreudFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicHefeFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicHudsonFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicInkwellFilter;
import cn.xag.xagclientmultimefialib.fliter.MagicN1977Filter;
import cn.xag.xagclientmultimefialib.fliter.MagicNashvilleFilter;

public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(MagicFilterType type, Context context) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case ANTIQUE:
                return new MagicAntiqueFilter(context);
            case BRANNAN:
                return new MagicBrannanFilter(context);
            case FREUD:
                return new MagicFreudFilter(context);
            case HEFE:
                return new MagicHefeFilter(context);
            case HUDSON:
                return new MagicHudsonFilter(context);
            case INKWELL:
                return new MagicInkwellFilter(context);
            case N1977:
                return new MagicN1977Filter(context);
            case NASHVILLE:
                return new MagicNashvilleFilter(context);
            case COOL:
                return new MagicCoolFilter(context);
            case WARM:
                return new MagicWarmFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static class MagicWarmFilter extends GPUImageFilter {
    }
}
