package com.youz.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.youz.android.R;

import java.util.Random;

public class UtilUserAvatar {
    static int[] avatarArray = new int[]
            {
                    R.drawable.avatar_000, R.drawable.avatar_001, R.drawable.avatar_002, R.drawable.avatar_003, R.drawable.avatar_004, R.drawable.avatar_005, R.drawable.avatar_006, R.drawable.avatar_007, R.drawable.avatar_008, R.drawable.avatar_009,
                    R.drawable.avatar_010, R.drawable.avatar_011, R.drawable.avatar_012, R.drawable.avatar_013, R.drawable.avatar_014, R.drawable.avatar_015, R.drawable.avatar_016, R.drawable.avatar_017, R.drawable.avatar_018, R.drawable.avatar_019,
                    R.drawable.avatar_020, R.drawable.avatar_021, R.drawable.avatar_022, R.drawable.avatar_023, R.drawable.avatar_024, R.drawable.avatar_025, R.drawable.avatar_026, R.drawable.avatar_027, R.drawable.avatar_028, R.drawable.avatar_029,
                    R.drawable.avatar_030, R.drawable.avatar_031, R.drawable.avatar_032, R.drawable.avatar_033, R.drawable.avatar_034, R.drawable.avatar_035, R.drawable.avatar_036, R.drawable.avatar_037, R.drawable.avatar_038, R.drawable.avatar_039,
                    R.drawable.avatar_040, R.drawable.avatar_041, R.drawable.avatar_042, R.drawable.avatar_043, R.drawable.avatar_044, R.drawable.avatar_045, R.drawable.avatar_046, R.drawable.avatar_047, R.drawable.avatar_048, R.drawable.avatar_049,
                    R.drawable.avatar_050, R.drawable.avatar_051, R.drawable.avatar_052, R.drawable.avatar_053, R.drawable.avatar_054, R.drawable.avatar_055, R.drawable.avatar_056, R.drawable.avatar_057, R.drawable.avatar_058, R.drawable.avatar_059,
                    R.drawable.avatar_060, R.drawable.avatar_061, R.drawable.avatar_062, R.drawable.avatar_063, R.drawable.avatar_064, R.drawable.avatar_065, R.drawable.avatar_066, R.drawable.avatar_067, R.drawable.avatar_068, R.drawable.avatar_069,
                    R.drawable.avatar_070, R.drawable.avatar_071, R.drawable.avatar_072, R.drawable.avatar_073, R.drawable.avatar_074, R.drawable.avatar_075, R.drawable.avatar_076, R.drawable.avatar_077, R.drawable.avatar_078, R.drawable.avatar_079,
                    R.drawable.avatar_080, R.drawable.avatar_081, R.drawable.avatar_082, R.drawable.avatar_083, R.drawable.avatar_084, R.drawable.avatar_085, R.drawable.avatar_086, R.drawable.avatar_087, R.drawable.avatar_088, R.drawable.avatar_089,
                    R.drawable.avatar_090, R.drawable.avatar_091, R.drawable.avatar_092, R.drawable.avatar_093, R.drawable.avatar_094, R.drawable.avatar_095, R.drawable.avatar_096, R.drawable.avatar_097, R.drawable.avatar_098, R.drawable.avatar_099,
                    R.drawable.avatar_100, R.drawable.avatar_101, R.drawable.avatar_102, R.drawable.avatar_103, R.drawable.avatar_104, R.drawable.avatar_105, R.drawable.avatar_106, R.drawable.avatar_107, R.drawable.avatar_108, R.drawable.avatar_109,
                    R.drawable.avatar_110, R.drawable.avatar_111, R.drawable.avatar_112, R.drawable.avatar_113, R.drawable.avatar_114, R.drawable.avatar_115, R.drawable.avatar_116, R.drawable.avatar_117, R.drawable.avatar_118, R.drawable.avatar_119,
                    R.drawable.avatar_120, R.drawable.avatar_121, R.drawable.avatar_122, R.drawable.avatar_123, R.drawable.avatar_124, R.drawable.avatar_125, R.drawable.avatar_126, R.drawable.avatar_127, R.drawable.avatar_128, R.drawable.avatar_129,
                    R.drawable.avatar_130, R.drawable.avatar_131, R.drawable.avatar_132, R.drawable.avatar_133, R.drawable.avatar_134, R.drawable.avatar_135, R.drawable.avatar_136, R.drawable.avatar_137, R.drawable.avatar_138, R.drawable.avatar_139,
                    R.drawable.avatar_140, R.drawable.avatar_141, R.drawable.avatar_142, R.drawable.avatar_143, R.drawable.avatar_144, R.drawable.avatar_145, R.drawable.avatar_146, R.drawable.avatar_147, R.drawable.avatar_148, R.drawable.avatar_149,
                    R.drawable.avatar_150, R.drawable.avatar_151, R.drawable.avatar_152, R.drawable.avatar_153, R.drawable.avatar_154, R.drawable.avatar_155, R.drawable.avatar_156, R.drawable.avatar_157, R.drawable.avatar_158, R.drawable.avatar_159,
                    R.drawable.avatar_160, R.drawable.avatar_161, R.drawable.avatar_162, R.drawable.avatar_163, R.drawable.avatar_164, R.drawable.avatar_165, R.drawable.avatar_166, R.drawable.avatar_167, R.drawable.avatar_168, R.drawable.avatar_169,
                    R.drawable.avatar_170, R.drawable.avatar_171, R.drawable.avatar_172, R.drawable.avatar_173, R.drawable.avatar_174, R.drawable.avatar_175, R.drawable.avatar_176, R.drawable.avatar_177, R.drawable.avatar_178, R.drawable.avatar_179,
                    R.drawable.avatar_180, R.drawable.avatar_181, R.drawable.avatar_182, R.drawable.avatar_183, R.drawable.avatar_184, R.drawable.avatar_185, R.drawable.avatar_186, R.drawable.avatar_187, R.drawable.avatar_188, R.drawable.avatar_189,
                    R.drawable.avatar_190, R.drawable.avatar_191, R.drawable.avatar_192, R.drawable.avatar_193, R.drawable.avatar_194, R.drawable.avatar_195, R.drawable.avatar_196, R.drawable.avatar_197, R.drawable.avatar_198, R.drawable.avatar_199,
                    R.drawable.avatar_200, R.drawable.avatar_201, R.drawable.avatar_202, R.drawable.avatar_203, R.drawable.avatar_204, R.drawable.avatar_205, R.drawable.avatar_206, R.drawable.avatar_207, R.drawable.avatar_208, R.drawable.avatar_209,
                    R.drawable.avatar_210, R.drawable.avatar_211, R.drawable.avatar_212, R.drawable.avatar_213, R.drawable.avatar_214, R.drawable.avatar_215, R.drawable.avatar_216, R.drawable.avatar_217, R.drawable.avatar_218, R.drawable.avatar_219,
                    R.drawable.avatar_220, R.drawable.avatar_221, R.drawable.avatar_222, R.drawable.avatar_223, R.drawable.avatar_224, R.drawable.avatar_225, R.drawable.avatar_226, R.drawable.avatar_227, R.drawable.avatar_228, R.drawable.avatar_229,
                    R.drawable.avatar_230, R.drawable.avatar_231, R.drawable.avatar_232, R.drawable.avatar_233, R.drawable.avatar_234, R.drawable.avatar_235, R.drawable.avatar_236, R.drawable.avatar_237, R.drawable.avatar_238, R.drawable.avatar_239,
                    R.drawable.avatar_240, R.drawable.avatar_241, R.drawable.avatar_242, R.drawable.avatar_243, R.drawable.avatar_244, R.drawable.avatar_245, R.drawable.avatar_246, R.drawable.avatar_247, R.drawable.avatar_248, R.drawable.avatar_249,
                    R.drawable.avatar_250, R.drawable.avatar_251, R.drawable.avatar_252, R.drawable.avatar_253, R.drawable.avatar_254, R.drawable.avatar_255, R.drawable.avatar_256, R.drawable.avatar_257, R.drawable.avatar_258, R.drawable.avatar_259,
                    R.drawable.avatar_260, R.drawable.avatar_261, R.drawable.avatar_262, R.drawable.avatar_263, R.drawable.avatar_264, R.drawable.avatar_265, R.drawable.avatar_266, R.drawable.avatar_267, R.drawable.avatar_268, R.drawable.avatar_269,
                    R.drawable.avatar_270, R.drawable.avatar_271, R.drawable.avatar_272, R.drawable.avatar_273, R.drawable.avatar_274, R.drawable.avatar_275, R.drawable.avatar_276, R.drawable.avatar_277, R.drawable.avatar_278, R.drawable.avatar_279,
                    R.drawable.avatar_280, R.drawable.avatar_281, R.drawable.avatar_282, R.drawable.avatar_283, R.drawable.avatar_284, R.drawable.avatar_285, R.drawable.avatar_286, R.drawable.avatar_287, R.drawable.avatar_288, R.drawable.avatar_289,
                    R.drawable.avatar_290, R.drawable.avatar_291, R.drawable.avatar_292, R.drawable.avatar_293, R.drawable.avatar_294, R.drawable.avatar_295, R.drawable.avatar_296, R.drawable.avatar_297, R.drawable.avatar_298, R.drawable.avatar_299,
                    R.drawable.avatar_300, R.drawable.avatar_301, R.drawable.avatar_302, R.drawable.avatar_303, R.drawable.avatar_304, R.drawable.avatar_305, R.drawable.avatar_306, R.drawable.avatar_307, R.drawable.avatar_308, R.drawable.avatar_309,
                    R.drawable.avatar_310, R.drawable.avatar_311, R.drawable.avatar_312, R.drawable.avatar_313, R.drawable.avatar_314, R.drawable.avatar_315, R.drawable.avatar_316, R.drawable.avatar_317, R.drawable.avatar_318, R.drawable.avatar_319,
                    R.drawable.avatar_320, R.drawable.avatar_321, R.drawable.avatar_322, R.drawable.avatar_323, R.drawable.avatar_324, R.drawable.avatar_325, R.drawable.avatar_326, R.drawable.avatar_327, R.drawable.avatar_328, R.drawable.avatar_329,
                    R.drawable.avatar_330, R.drawable.avatar_331, R.drawable.avatar_332, R.drawable.avatar_333, R.drawable.avatar_334, R.drawable.avatar_335, R.drawable.avatar_336, R.drawable.avatar_337, R.drawable.avatar_338, R.drawable.avatar_339,
                    R.drawable.avatar_340, R.drawable.avatar_341, R.drawable.avatar_342, R.drawable.avatar_343, R.drawable.avatar_344, R.drawable.avatar_345, R.drawable.avatar_346, R.drawable.avatar_347, R.drawable.avatar_348, R.drawable.avatar_349,
                    R.drawable.avatar_350, R.drawable.avatar_351, R.drawable.avatar_352, R.drawable.avatar_353, R.drawable.avatar_354, R.drawable.avatar_355, R.drawable.avatar_356, R.drawable.avatar_357, R.drawable.avatar_358, R.drawable.avatar_359,
                    R.drawable.avatar_360, R.drawable.avatar_361, R.drawable.avatar_362, R.drawable.avatar_363, R.drawable.avatar_364, R.drawable.avatar_365, R.drawable.avatar_366, R.drawable.avatar_367, R.drawable.avatar_368, R.drawable.avatar_369,
                    R.drawable.avatar_370, R.drawable.avatar_371, R.drawable.avatar_372, R.drawable.avatar_373, R.drawable.avatar_374, R.drawable.avatar_375, R.drawable.avatar_376, R.drawable.avatar_377, R.drawable.avatar_378, R.drawable.avatar_379,
                    R.drawable.avatar_380, R.drawable.avatar_381, R.drawable.avatar_382, R.drawable.avatar_383, R.drawable.avatar_384, R.drawable.avatar_385, R.drawable.avatar_386, R.drawable.avatar_387, R.drawable.avatar_388, R.drawable.avatar_389,
                    R.drawable.avatar_390, R.drawable.avatar_391, R.drawable.avatar_392, R.drawable.avatar_393, R.drawable.avatar_394, R.drawable.avatar_395, R.drawable.avatar_396, R.drawable.avatar_397, R.drawable.avatar_398, R.drawable.avatar_399,
                    R.drawable.avatar_400, R.drawable.avatar_401, R.drawable.avatar_402, R.drawable.avatar_403, R.drawable.avatar_404, R.drawable.avatar_405, R.drawable.avatar_406, R.drawable.avatar_407, R.drawable.avatar_408, R.drawable.avatar_409,
                    R.drawable.avatar_410, R.drawable.avatar_411, R.drawable.avatar_412, R.drawable.avatar_413, R.drawable.avatar_414, R.drawable.avatar_415, R.drawable.avatar_416, R.drawable.avatar_417, R.drawable.avatar_418, R.drawable.avatar_419,
                    R.drawable.avatar_420, R.drawable.avatar_421, R.drawable.avatar_422, R.drawable.avatar_423, R.drawable.avatar_424, R.drawable.avatar_425, R.drawable.avatar_426, R.drawable.avatar_427, R.drawable.avatar_428, R.drawable.avatar_429,
                    R.drawable.avatar_430, R.drawable.avatar_431, R.drawable.avatar_432, R.drawable.avatar_433, R.drawable.avatar_434, R.drawable.avatar_435, R.drawable.avatar_436, R.drawable.avatar_437, R.drawable.avatar_438, R.drawable.avatar_439,
                    R.drawable.avatar_440, R.drawable.avatar_441, R.drawable.avatar_442, R.drawable.avatar_443, R.drawable.avatar_444, R.drawable.avatar_445, R.drawable.avatar_446, R.drawable.avatar_447, R.drawable.avatar_448, R.drawable.avatar_449,
                    R.drawable.avatar_450, R.drawable.avatar_451, R.drawable.avatar_452, R.drawable.avatar_453, R.drawable.avatar_454, R.drawable.avatar_455, R.drawable.avatar_456, R.drawable.avatar_457, R.drawable.avatar_458, R.drawable.avatar_459,
                    R.drawable.avatar_460, R.drawable.avatar_461, R.drawable.avatar_462, R.drawable.avatar_463, R.drawable.avatar_464, R.drawable.avatar_465, R.drawable.avatar_466, R.drawable.avatar_467, R.drawable.avatar_468, R.drawable.avatar_469,
                    R.drawable.avatar_470, R.drawable.avatar_471, R.drawable.avatar_472, R.drawable.avatar_473, R.drawable.avatar_474, R.drawable.avatar_475, R.drawable.avatar_476, R.drawable.avatar_477, R.drawable.avatar_478, R.drawable.avatar_479,
                    R.drawable.avatar_480, R.drawable.avatar_481, R.drawable.avatar_482, R.drawable.avatar_483, R.drawable.avatar_484, R.drawable.avatar_485, R.drawable.avatar_486, R.drawable.avatar_487, R.drawable.avatar_488, R.drawable.avatar_489,
                    R.drawable.avatar_490, R.drawable.avatar_491, R.drawable.avatar_492, R.drawable.avatar_493, R.drawable.avatar_494, R.drawable.avatar_495, R.drawable.avatar_496, R.drawable.avatar_497, R.drawable.avatar_498, R.drawable.avatar_499,
                    R.drawable.avatar_500, R.drawable.avatar_501, R.drawable.avatar_502, R.drawable.avatar_503, R.drawable.avatar_504, R.drawable.avatar_505, R.drawable.avatar_506, R.drawable.avatar_507, R.drawable.avatar_508, R.drawable.avatar_509,
                    R.drawable.avatar_510, R.drawable.avatar_511, R.drawable.avatar_512, R.drawable.avatar_513, R.drawable.avatar_514, R.drawable.avatar_515, R.drawable.avatar_516, R.drawable.avatar_517, R.drawable.avatar_518, R.drawable.avatar_519,
                    R.drawable.avatar_520, R.drawable.avatar_521, R.drawable.avatar_522, R.drawable.avatar_523, R.drawable.avatar_524, R.drawable.avatar_525, R.drawable.avatar_526, R.drawable.avatar_527, R.drawable.avatar_528, R.drawable.avatar_529,
                    R.drawable.avatar_530, R.drawable.avatar_531, R.drawable.avatar_532, R.drawable.avatar_533, R.drawable.avatar_534, R.drawable.avatar_535, R.drawable.avatar_536, R.drawable.avatar_537, R.drawable.avatar_538, R.drawable.avatar_539,
                    R.drawable.avatar_540, R.drawable.avatar_541, R.drawable.avatar_542, R.drawable.avatar_543, R.drawable.avatar_544, R.drawable.avatar_545, R.drawable.avatar_546, R.drawable.avatar_547, R.drawable.avatar_548, R.drawable.avatar_549,
                    R.drawable.avatar_550, R.drawable.avatar_551, R.drawable.avatar_552, R.drawable.avatar_553, R.drawable.avatar_554, R.drawable.avatar_555, R.drawable.avatar_556, R.drawable.avatar_557, R.drawable.avatar_558, R.drawable.avatar_559,
                    R.drawable.avatar_560, R.drawable.avatar_561, R.drawable.avatar_562, R.drawable.avatar_563, R.drawable.avatar_564, R.drawable.avatar_565, R.drawable.avatar_566, R.drawable.avatar_567, R.drawable.avatar_568, R.drawable.avatar_569,
                    R.drawable.avatar_570, R.drawable.avatar_571, R.drawable.avatar_572, R.drawable.avatar_573, R.drawable.avatar_574, R.drawable.avatar_575, R.drawable.avatar_576, R.drawable.avatar_577, R.drawable.avatar_578, R.drawable.avatar_579,
                    R.drawable.avatar_580, R.drawable.avatar_581, R.drawable.avatar_582, R.drawable.avatar_583, R.drawable.avatar_584, R.drawable.avatar_585, R.drawable.avatar_586, R.drawable.avatar_587, R.drawable.avatar_588, R.drawable.avatar_589,
                    R.drawable.avatar_590, R.drawable.avatar_591, R.drawable.avatar_592, R.drawable.avatar_593, R.drawable.avatar_594, R.drawable.avatar_595, R.drawable.avatar_596, R.drawable.avatar_597, R.drawable.avatar_598, R.drawable.avatar_599,
                    R.drawable.avatar_600, R.drawable.avatar_601, R.drawable.avatar_602, R.drawable.avatar_603, R.drawable.avatar_604, R.drawable.avatar_605, R.drawable.avatar_606, R.drawable.avatar_607, R.drawable.avatar_608, R.drawable.avatar_609,
                    R.drawable.avatar_610, R.drawable.avatar_611
            };

    public static int getDrawableRes(Context context, String userID) {
        SharedPreferences prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getInt("Secret" + userID, -1) != -1) {
            return prefs.getInt("Secret" + userID, -1);
        } else {
            Random random = new Random();
            int index = random.nextInt(avatarArray.length);
            int res = avatarArray[index];
            editor.putInt("Secret" + userID, res);
            editor.commit();

            return res;
        }
    }

}