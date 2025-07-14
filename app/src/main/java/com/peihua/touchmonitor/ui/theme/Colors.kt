package com.peihua.touchmonitor.ui.theme

import androidx.compose.ui.graphics.Color


abstract class ColorSwitch<T>(
    val primary: Long,
    val name: String = "",
    val switch: Map<T, Color> = mapOf(),
) {
    open operator fun get(key: T?): Color {
        if (key == null) return Color(primary)
        return switch[key] ?: Color(primary)
    }
}

class MaterialColor(primary: Long, name: String = "", switch: Map<Int, Color> = mapOf()) :
    ColorSwitch<Int>(primary, name, switch)


object Colors {

    private const val _redPrimaryValue = 0xFFF44336
    val Red = MaterialColor(
        _redPrimaryValue, "Red", mapOf(
            50 to Color(0xFFFFEBEE),
            100 to Color(0xFFFFCDD2),
            200 to Color(0xFFEF9A9A),
            300 to Color(0xFFE57373),
            400 to Color(0xFFEF5350),
            500 to Color(_redPrimaryValue),
            600 to Color(0xFFE53935),
            700 to Color(0xFFD32F2F),
            800 to Color(0xFFC62828),
            900 to Color(0xFFB71C1C),
        )
    )
    val Pink = MaterialColor(
        _pinkPrimaryValue,
        "Pink",
        mapOf(
            50 to Color(0xFFFCE4EC),
            100 to Color(0xFFF8BBD0),
            200 to Color(0xFFF48FB1),
            300 to Color(0xFFF06292),
            400 to Color(0xFFEC407A),
            500 to Color(_pinkPrimaryValue),
            600 to Color(0xFFD81B60),
            700 to Color(0xFFC2185B),
            800 to Color(0xFFAD1457),
            900 to Color(0xFF880E4F),
        ),
    );
    private const val _pinkPrimaryValue = 0xFFE91E63;
    val Purple = MaterialColor(
        _purplePrimaryValue,
        "Purple",
        mapOf(
            50 to Color(0xFFF3E5F5),
            100 to Color(0xFFE1BEE7),
            200 to Color(0xFFCE93D8),
            300 to Color(0xFFBA68C8),
            400 to Color(0xFFAB47BC),
            500 to Color(_purplePrimaryValue),
            600 to Color(0xFF8E24AA),
            700 to Color(0xFF7B1FA2),
            800 to Color(0xFF6A1B9A),
            900 to Color(0xFF4A148C),
        ),
    );
    private const val _purplePrimaryValue = 0xFF9C27B0;
    val DeepPurple = MaterialColor(
        _deepPurplePrimaryValue,
        "DeepPurple",
        mapOf(
            50 to Color(0xFFEDE7F6),
            100 to Color(0xFFD1C4E9),
            200 to Color(0xFFB39DDB),
            300 to Color(0xFF9575CD),
            400 to Color(0xFF7E57C2),
            500 to Color(_deepPurplePrimaryValue),
            600 to Color(0xFF5E35B1),
            700 to Color(0xFF512DA8),
            800 to Color(0xFF4527A0),
            900 to Color(0xFF311B92),
        ),
    );
    private const val _deepPurplePrimaryValue = 0xFF673AB7;
    val Indigo = MaterialColor(
        _indigoPrimaryValue,
        "Indigo",
        mapOf(
            50 to Color(0xFFE8EAF6),
            100 to Color(0xFFC5CAE9),
            200 to Color(0xFF9FA8DA),
            300 to Color(0xFF7986CB),
            400 to Color(0xFF5C6BC0),
            500 to Color(_indigoPrimaryValue),
            600 to Color(0xFF3949AB),
            700 to Color(0xFF303F9F),
            800 to Color(0xFF283593),
            900 to Color(0xFF1A237E),
        ),
    );
    private const val _indigoPrimaryValue = 0xFF3F51B5;
    private const val _bluePrimaryValue = 0xFF2196F3;
    val Blue = MaterialColor(
        _bluePrimaryValue,
        "Blue",
        mapOf(
            50 to Color(0xFFE3F2FD),
            100 to Color(0xFFBBDEFB),
            200 to Color(0xFF90CAF9),
            300 to Color(0xFF64B5F6),
            400 to Color(0xFF42A5F5),
            500 to Color(_bluePrimaryValue),
            600 to Color(0xFF1E88E5),
            700 to Color(0xFF1976D2),
            800 to Color(0xFF1565C0),
            900 to Color(0xFF0D47A1),
        )
    )
    val LightBlue = MaterialColor(
        _lightBluePrimaryValue,
        "LightBlue",
        mapOf(
            50 to Color(0xFFE1F5FE),
            100 to Color(0xFFB3E5FC),
            200 to Color(0xFF81D4FA),
            300 to Color(0xFF4FC3F7),
            400 to Color(0xFF29B6F6),
            500 to Color(_lightBluePrimaryValue),
            600 to Color(0xFF039BE5),
            700 to Color(0xFF0288D1),
            800 to Color(0xFF0277BD),
            900 to Color(0xFF01579B),
        ),
    );
    private const val _lightBluePrimaryValue = 0xFF03A9F4;
    val Cyan = MaterialColor(
        _cyanPrimaryValue,
        "Cyan",
        mapOf(
            50 to Color(0xFFE0F7FA),
            100 to Color(0xFFB2EBF2),
            200 to Color(0xFF80DEEA),
            300 to Color(0xFF4DD0E1),
            400 to Color(0xFF26C6DA),
            500 to Color(_cyanPrimaryValue),
            600 to Color(0xFF00ACC1),
            700 to Color(0xFF0097A7),
            800 to Color(0xFF00838F),
            900 to Color(0xFF006064),
        ),
    );
    private const val _cyanPrimaryValue = 0xFF00BCD4;

    val Teal = MaterialColor(
        _tealPrimaryValue,
        "Teal",
        mapOf(
            50 to Color(0xFFE0F2F1),
            100 to Color(0xFFB2DFDB),
            200 to Color(0xFF80CBC4),
            300 to Color(0xFF4DB6AC),
            400 to Color(0xFF26A69A),
            500 to Color(_tealPrimaryValue),
            600 to Color(0xFF00897B),
            700 to Color(0xFF00796B),
            800 to Color(0xFF00695C),
            900 to Color(0xFF004D40),
        ),
    );
    private const val _tealPrimaryValue = 0xFF009688;


    val Green = MaterialColor(
        _greenPrimaryValue,
        "Green",
        mapOf(
            50 to Color(0xFFE8F5E9),
            100 to Color(0xFFC8E6C9),
            200 to Color(0xFFA5D6A7),
            300 to Color(0xFF81C784),
            400 to Color(0xFF66BB6A),
            500 to Color(_greenPrimaryValue),
            600 to Color(0xFF43A047),
            700 to Color(0xFF388E3C),
            800 to Color(0xFF2E7D32),
            900 to Color(0xFF1B5E20),
        ),
    );
    private const val _greenPrimaryValue = 0xFF4CAF50;

    val LightGreen = MaterialColor(
        _lightGreenPrimaryValue,
        "LightGreen",
        mapOf(
            50 to Color(0xFFF1F8E9),
            100 to Color(0xFFDCEDC8),
            200 to Color(0xFFC5E1A5),
            300 to Color(0xFFAED581),
            400 to Color(0xFF9CCC65),
            500 to Color(_lightGreenPrimaryValue),
            600 to Color(0xFF7CB342),
            700 to Color(0xFF689F38),
            800 to Color(0xFF558B2F),
            900 to Color(0xFF33691E),
        ),
    );
    private const val _lightGreenPrimaryValue = 0xFF8BC34A;


    val Lime = MaterialColor(
        _limePrimaryValue,
        "Lime",
        mapOf(
            50 to Color(0xFFF9FBE7),
            100 to Color(0xFFF0F4C3),
            200 to Color(0xFFE6EE9C),
            300 to Color(0xFFDCE775),
            400 to Color(0xFFD4E157),
            500 to Color(_limePrimaryValue),
            600 to Color(0xFFC0CA33),
            700 to Color(0xFFAFB42B),
            800 to Color(0xFF9E9D24),
            900 to Color(0xFF827717),
        ),
    );
    private const val _limePrimaryValue = 0xFFCDDC39;

    val Yellow = MaterialColor(
        _yellowPrimaryValue,
        "Yellow",
        mapOf(
            50 to Color(0xFFFFFDE7),
            100 to Color(0xFFFFF9C4),
            200 to Color(0xFFFFF59D),
            300 to Color(0xFFFFF176),
            400 to Color(0xFFFFEE58),
            500 to Color(_yellowPrimaryValue),
            600 to Color(0xFFFDD835),
            700 to Color(0xFFFBC02D),
            800 to Color(0xFFF9A825),
            900 to Color(0xFFF57F17),
        ),
    );
    private const val _yellowPrimaryValue = 0xFFFFEB3B;

    val Amber = MaterialColor(
        _amberPrimaryValue,
        "Amber",
        mapOf(
            50 to Color(0xFFFFF8E1),
            100 to Color(0xFFFFECB3),
            200 to Color(0xFFFFE082),
            300 to Color(0xFFFFD54F),
            400 to Color(0xFFFFCA28),
            500 to Color(_amberPrimaryValue),
            600 to Color(0xFFFFB300),
            700 to Color(0xFFFFA000),
            800 to Color(0xFFFF8F00),
            900 to Color(0xFFFF6F00),
        ),
    );
    private const val _amberPrimaryValue = 0xFFFFC107;

    val Orange = MaterialColor(
        _orangePrimaryValue,
        "Orange",
        mapOf(
            50 to Color(0xFFFFF3E0),
            100 to Color(0xFFFFE0B2),
            200 to Color(0xFFFFCC80),
            300 to Color(0xFFFFB74D),
            400 to Color(0xFFFFA726),
            500 to Color(_orangePrimaryValue),
            600 to Color(0xFFFB8C00),
            700 to Color(0xFFF57C00),
            800 to Color(0xFFEF6C00),
            900 to Color(0xFFE65100),
        ),
    );
    private const val _orangePrimaryValue = 0xFFFF9800;

    val DeepOrange = MaterialColor(
        _deepOrangePrimaryValue,
        "DeepOrange",
        mapOf(
            50 to Color(0xFFFBE9E7),
            100 to Color(0xFFFFCCBC),
            200 to Color(0xFFFFAB91),
            300 to Color(0xFFFF8A65),
            400 to Color(0xFFFF7043),
            500 to Color(_deepOrangePrimaryValue),
            600 to Color(0xFFF4511E),
            700 to Color(0xFFE64A19),
            800 to Color(0xFFD84315),
            900 to Color(0xFFBF360C),
        ),
    );
    private const val _deepOrangePrimaryValue = 0xFFFF5722;

    val Brown = MaterialColor(
        _brownPrimaryValue,
        "Brown",
        mapOf(
            50 to Color(0xFFEFEBE9),
            100 to Color(0xFFD7CCC8),
            200 to Color(0xFFBCAAA4),
            300 to Color(0xFFA1887F),
            400 to Color(0xFF8D6E63),
            500 to Color(_brownPrimaryValue),
            600 to Color(0xFF6D4C41),
            700 to Color(0xFF5D4037),
            800 to Color(0xFF4E342E),
            900 to Color(0xFF3E2723),
        ),
    );
    private const val _brownPrimaryValue = 0xFF795548;

    val Grey = MaterialColor(
        _greyPrimaryValue,
        "Grey",
        mapOf(
            50 to Color(0xFFFAFAFA),
            100 to Color(0xFFF5F5F5),
            200 to Color(0xFFEEEEEE),
            300 to Color(0xFFE0E0E0),
            350 to Color(0xFFD6D6D6), // only for raised button while pressed in light theme
            400 to Color(0xFFBDBDBD),
            500 to Color(_greyPrimaryValue),
            600 to Color(0xFF757575),
            700 to Color(0xFF616161),
            800 to Color(0xFF424242),
            850 to Color(0xFF303030), // only for background color in dark theme
            900 to Color(0xFF212121),
        ),
    );
    private const val _greyPrimaryValue = 0xFF9E9E9E;

    val BlueGrey = MaterialColor(
        _blueGreyPrimaryValue,
        "BlueGrey",
        mapOf(
            50 to Color(0xFFECEFF1),
            100 to Color(0xFFCFD8DC),
            200 to Color(0xFFB0BEC5),
            300 to Color(0xFF90A4AE),
            400 to Color(0xFF78909C),
            500 to Color(_blueGreyPrimaryValue),
            600 to Color(0xFF546E7A),
            700 to Color(0xFF455A64),
            800 to Color(0xFF37474F),
            900 to Color(0xFF263238),
        ),
    );
    private const val _blueGreyPrimaryValue = 0xFF607D8B;
}


