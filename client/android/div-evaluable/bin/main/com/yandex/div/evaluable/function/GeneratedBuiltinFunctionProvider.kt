package com.yandex.div.evaluable.function

import com.yandex.div.evaluable.EvaluableException
import com.yandex.div.evaluable.EvaluableType
import com.yandex.div.evaluable.Function
import com.yandex.div.evaluable.FunctionProvider
import kotlin.String
import kotlin.collections.List

public object GeneratedBuiltinFunctionProvider : FunctionProvider {
  override fun `get`(name: String, args: List<EvaluableType>): Function {
    when (name) {
      "abs" -> {
        if (DoubleAbs.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleAbs
        }
        if (IntegerAbs.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerAbs
        }
        if (DoubleAbs.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleAbs
        }
        if (IntegerAbs.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerAbs
        }
        throw getFunctionArgumentsException(name, args)
      }
      "addMillis" -> {
        return AddMillis.withArgumentsValidation(args)
      }
      "argb" -> {
        return ColorArgb.withArgumentsValidation(args)
      }
      "ceil" -> {
        return DoubleCeil.withArgumentsValidation(args)
      }
      "contains" -> {
        return StringContains.withArgumentsValidation(args)
      }
      "copySign" -> {
        if (DoubleCopySign.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleCopySign
        }
        if (IntegerCopySign.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerCopySign
        }
        if (DoubleCopySign.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleCopySign
        }
        if (IntegerCopySign.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerCopySign
        }
        throw getFunctionArgumentsException(name, args)
      }
      "decodeUri" -> {
        return StringDecodeUri.withArgumentsValidation(args)
      }
      "div" -> {
        if (DoubleDiv.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleDiv
        }
        if (IntegerDiv.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerDiv
        }
        if (DoubleDiv.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleDiv
        }
        if (IntegerDiv.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerDiv
        }
        throw getFunctionArgumentsException(name, args)
      }
      "encodeRegex" -> {
        return EncodeRegex.withArgumentsValidation(args)
      }
      "encodeUri" -> {
        return StringEncodeUri.withArgumentsValidation(args)
      }
      "floor" -> {
        return DoubleFloor.withArgumentsValidation(args)
      }
      "formatDateAsLocal" -> {
        return FormatDateAsLocal.withArgumentsValidation(args)
      }
      "formatDateAsLocalWithLocale" -> {
        return FormatDateAsLocalWithLocale.withArgumentsValidation(args)
      }
      "formatDateAsUTC" -> {
        return FormatDateAsUTC.withArgumentsValidation(args)
      }
      "formatDateAsUTCWithLocale" -> {
        return FormatDateAsUTCWithLocale.withArgumentsValidation(args)
      }
      "getArrayBoolean" -> {
        return GetArrayBoolean.withArgumentsValidation(args)
      }
      "getArrayColor" -> {
        return GetArrayColor.withArgumentsValidation(args)
      }
      "getArrayFromArray" -> {
        return GetArrayFromArray.withArgumentsValidation(args)
      }
      "getArrayFromDict" -> {
        return GetArrayFromDict.withArgumentsValidation(args)
      }
      "getArrayInteger" -> {
        return GetArrayInteger.withArgumentsValidation(args)
      }
      "getArrayNumber" -> {
        return GetArrayNumber.withArgumentsValidation(args)
      }
      "getArrayOptBoolean" -> {
        return GetArrayOptBoolean.withArgumentsValidation(args)
      }
      "getArrayOptColor" -> {
        if (GetArrayOptColorWithColorFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArrayOptColorWithColorFallback
        }
        if (GetArrayOptColorWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArrayOptColorWithStringFallback
        }
        if (GetArrayOptColorWithColorFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetArrayOptColorWithColorFallback
        }
        if (GetArrayOptColorWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetArrayOptColorWithStringFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getArrayOptInteger" -> {
        return GetArrayOptInteger.withArgumentsValidation(args)
      }
      "getArrayOptNumber" -> {
        return GetArrayOptNumber.withArgumentsValidation(args)
      }
      "getArrayOptString" -> {
        return GetArrayOptString.withArgumentsValidation(args)
      }
      "getArrayOptUrl" -> {
        if (GetArrayOptUrlWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArrayOptUrlWithStringFallback
        }
        if (GetArrayOptUrlWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArrayOptUrlWithUrlFallback
        }
        if (GetArrayOptUrlWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetArrayOptUrlWithStringFallback
        }
        if (GetArrayOptUrlWithUrlFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetArrayOptUrlWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getArrayString" -> {
        return GetArrayString.withArgumentsValidation(args)
      }
      "getArrayUrl" -> {
        return GetArrayUrl.withArgumentsValidation(args)
      }
      "getBooleanFromArray" -> {
        return GetBooleanFromArray.withArgumentsValidation(args)
      }
      "getBooleanFromDict" -> {
        return GetBooleanFromDict.withArgumentsValidation(args)
      }
      "getBooleanValue" -> {
        return GetBooleanValue.withArgumentsValidation(args)
      }
      "getColorAlpha" -> {
        if (ColorAlphaComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorAlphaComponentGetter
        }
        if (ColorStringAlphaComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringAlphaComponentGetter
        }
        if (ColorAlphaComponentGetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorAlphaComponentGetter
        }
        if (ColorStringAlphaComponentGetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringAlphaComponentGetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getColorBlue" -> {
        if (ColorBlueComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorBlueComponentGetter
        }
        if (ColorStringBlueComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringBlueComponentGetter
        }
        if (ColorBlueComponentGetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorBlueComponentGetter
        }
        if (ColorStringBlueComponentGetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringBlueComponentGetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getColorFromArray" -> {
        return GetColorFromArray.withArgumentsValidation(args)
      }
      "getColorFromDict" -> {
        return GetColorFromDict.withArgumentsValidation(args)
      }
      "getColorGreen" -> {
        if (ColorGreenComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorGreenComponentGetter
        }
        if (ColorStringGreenComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringGreenComponentGetter
        }
        if (ColorGreenComponentGetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorGreenComponentGetter
        }
        if (ColorStringGreenComponentGetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringGreenComponentGetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getColorRed" -> {
        if (ColorRedComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorRedComponentGetter
        }
        if (ColorStringRedComponentGetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringRedComponentGetter
        }
        if (ColorRedComponentGetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorRedComponentGetter
        }
        if (ColorStringRedComponentGetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringRedComponentGetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getColorValue" -> {
        if (GetColorValue.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetColorValue
        }
        if (GetColorValueString.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetColorValueString
        }
        if (GetColorValue.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetColorValue
        }
        if (GetColorValueString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetColorValueString
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getDay" -> {
        return GetDay.withArgumentsValidation(args)
      }
      "getDayOfWeek" -> {
        return GetDayOfWeek.withArgumentsValidation(args)
      }
      "getDictBoolean" -> {
        return GetDictBoolean.withArgumentsValidation(args)
      }
      "getDictColor" -> {
        return GetDictColor.withArgumentsValidation(args)
      }
      "getDictFromArray" -> {
        return GetDictFromArray.withArgumentsValidation(args)
      }
      "getDictFromDict" -> {
        return GetDictFromDict.withArgumentsValidation(args)
      }
      "getDictInteger" -> {
        return GetDictInteger.withArgumentsValidation(args)
      }
      "getDictNumber" -> {
        return GetDictNumber.withArgumentsValidation(args)
      }
      "getDictOptBoolean" -> {
        return GetDictOptBoolean.withArgumentsValidation(args)
      }
      "getDictOptColor" -> {
        if (GetDictOptColorWithColorFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetDictOptColorWithColorFallback
        }
        if (GetDictOptColorWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetDictOptColorWithStringFallback
        }
        if (GetDictOptColorWithColorFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetDictOptColorWithColorFallback
        }
        if (GetDictOptColorWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetDictOptColorWithStringFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getDictOptInteger" -> {
        return GetDictOptInteger.withArgumentsValidation(args)
      }
      "getDictOptNumber" -> {
        return GetDictOptNumber.withArgumentsValidation(args)
      }
      "getDictOptString" -> {
        return GetDictOptString.withArgumentsValidation(args)
      }
      "getDictOptUrl" -> {
        if (GetDictOptUrlWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetDictOptUrlWithStringFallback
        }
        if (GetDictOptUrlWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetDictOptUrlWithUrlFallback
        }
        if (GetDictOptUrlWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetDictOptUrlWithStringFallback
        }
        if (GetDictOptUrlWithUrlFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetDictOptUrlWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getDictString" -> {
        return GetDictString.withArgumentsValidation(args)
      }
      "getDictUrl" -> {
        return GetDictUrl.withArgumentsValidation(args)
      }
      "getHours" -> {
        return GetHours.withArgumentsValidation(args)
      }
      "getIntegerFromArray" -> {
        return GetIntegerFromArray.withArgumentsValidation(args)
      }
      "getIntegerFromDict" -> {
        return GetIntegerFromDict.withArgumentsValidation(args)
      }
      "getIntegerValue" -> {
        return GetIntegerValue.withArgumentsValidation(args)
      }
      "getIntervalHours" -> {
        return GetIntervalHours.withArgumentsValidation(args)
      }
      "getIntervalMinutes" -> {
        return GetIntervalMinutes.withArgumentsValidation(args)
      }
      "getIntervalSeconds" -> {
        return GetIntervalSeconds.withArgumentsValidation(args)
      }
      "getIntervalTotalDays" -> {
        return GetIntervalTotalDays.withArgumentsValidation(args)
      }
      "getIntervalTotalHours" -> {
        return GetIntervalTotalHours.withArgumentsValidation(args)
      }
      "getIntervalTotalMinutes" -> {
        return GetIntervalTotalMinutes.withArgumentsValidation(args)
      }
      "getIntervalTotalSeconds" -> {
        return GetIntervalTotalSeconds.withArgumentsValidation(args)
      }
      "getIntervalTotalWeeks" -> {
        return GetIntervalTotalWeeks.withArgumentsValidation(args)
      }
      "getMillis" -> {
        return GetMillis.withArgumentsValidation(args)
      }
      "getMinutes" -> {
        return GetMinutes.withArgumentsValidation(args)
      }
      "getMonth" -> {
        return GetMonth.withArgumentsValidation(args)
      }
      "getNumberFromArray" -> {
        return GetNumberFromArray.withArgumentsValidation(args)
      }
      "getNumberFromDict" -> {
        return GetNumberFromDict.withArgumentsValidation(args)
      }
      "getNumberValue" -> {
        return GetNumberValue.withArgumentsValidation(args)
      }
      "getOptArrayFromArray" -> {
        return GetOptArrayFromArray.withArgumentsValidation(args)
      }
      "getOptArrayFromDict" -> {
        return GetOptArrayFromDict.withArgumentsValidation(args)
      }
      "getOptBooleanFromArray" -> {
        return GetOptBooleanFromArray.withArgumentsValidation(args)
      }
      "getOptBooleanFromDict" -> {
        return GetOptBooleanFromDict.withArgumentsValidation(args)
      }
      "getOptColorFromArray" -> {
        if (GetOptColorFromArrayWithColorFallback.matchesArguments(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromArrayWithColorFallback
        }
        if (GetOptColorFromArrayWithStringFallback.matchesArguments(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromArrayWithStringFallback
        }
        if (GetOptColorFromArrayWithColorFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromArrayWithColorFallback
        }
        if (GetOptColorFromArrayWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromArrayWithStringFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getOptColorFromDict" -> {
        if (GetOptColorFromDictWithColorFallback.matchesArguments(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromDictWithColorFallback
        }
        if (GetOptColorFromDictWithStringFallback.matchesArguments(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromDictWithStringFallback
        }
        if (GetOptColorFromDictWithColorFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromDictWithColorFallback
        }
        if (GetOptColorFromDictWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptColorFromDictWithStringFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getOptDictFromArray" -> {
        return GetOptDictFromArray.withArgumentsValidation(args)
      }
      "getOptDictFromDict" -> {
        return GetOptDictFromDict.withArgumentsValidation(args)
      }
      "getOptIntegerFromArray" -> {
        return GetOptIntegerFromArray.withArgumentsValidation(args)
      }
      "getOptIntegerFromDict" -> {
        return GetOptIntegerFromDict.withArgumentsValidation(args)
      }
      "getOptNumberFromArray" -> {
        return GetOptNumberFromArray.withArgumentsValidation(args)
      }
      "getOptNumberFromDict" -> {
        return GetOptNumberFromDict.withArgumentsValidation(args)
      }
      "getOptStringFromArray" -> {
        return GetOptStringFromArray.withArgumentsValidation(args)
      }
      "getOptStringFromDict" -> {
        return GetOptStringFromDict.withArgumentsValidation(args)
      }
      "getOptUrlFromArray" -> {
        if (GetOptUrlFromArrayWithStringFallback.matchesArguments(args) ==
            Function.MatchResult.Ok) {
          return GetOptUrlFromArrayWithStringFallback
        }
        if (GetOptUrlFromArrayWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetOptUrlFromArrayWithUrlFallback
        }
        if (GetOptUrlFromArrayWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptUrlFromArrayWithStringFallback
        }
        if (GetOptUrlFromArrayWithUrlFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptUrlFromArrayWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getOptUrlFromDict" -> {
        if (GetOptUrlFromDictWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetOptUrlFromDictWithStringFallback
        }
        if (GetOptUrlFromDictWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetOptUrlFromDictWithUrlFallback
        }
        if (GetOptUrlFromDictWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptUrlFromDictWithStringFallback
        }
        if (GetOptUrlFromDictWithUrlFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetOptUrlFromDictWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getSeconds" -> {
        return GetSeconds.withArgumentsValidation(args)
      }
      "getStoredArrayValue" -> {
        return GetStoredArrayValue.withArgumentsValidation(args)
      }
      "getStoredBooleanValue" -> {
        return GetStoredBooleanValue.withArgumentsValidation(args)
      }
      "getStoredColorValue" -> {
        if (GetStoredColorValue.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetStoredColorValue
        }
        if (GetStoredColorValueString.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetStoredColorValueString
        }
        if (GetStoredColorValue.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetStoredColorValue
        }
        if (GetStoredColorValueString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetStoredColorValueString
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getStoredDictValue" -> {
        return GetStoredDictValue.withArgumentsValidation(args)
      }
      "getStoredIntegerValue" -> {
        return GetStoredIntegerValue.withArgumentsValidation(args)
      }
      "getStoredNumberValue" -> {
        return GetStoredNumberValue.withArgumentsValidation(args)
      }
      "getStoredStringValue" -> {
        return GetStoredStringValue.withArgumentsValidation(args)
      }
      "getStoredUrlValue" -> {
        if (GetStoredUrlValueWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetStoredUrlValueWithStringFallback
        }
        if (GetStoredUrlValueWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetStoredUrlValueWithUrlFallback
        }
        if (GetStoredUrlValueWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetStoredUrlValueWithStringFallback
        }
        if (GetStoredUrlValueWithUrlFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetStoredUrlValueWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getStringFromArray" -> {
        return GetStringFromArray.withArgumentsValidation(args)
      }
      "getStringFromDict" -> {
        return GetStringFromDict.withArgumentsValidation(args)
      }
      "getStringValue" -> {
        return GetStringValue.withArgumentsValidation(args)
      }
      "getUrlFromArray" -> {
        return GetUrlFromArray.withArgumentsValidation(args)
      }
      "getUrlFromDict" -> {
        return GetUrlFromDict.withArgumentsValidation(args)
      }
      "getUrlValue" -> {
        if (GetUrlValueWithStringFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetUrlValueWithStringFallback
        }
        if (GetUrlValueWithUrlFallback.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetUrlValueWithUrlFallback
        }
        if (GetUrlValueWithStringFallback.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return GetUrlValueWithStringFallback
        }
        if (GetUrlValueWithUrlFallback.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetUrlValueWithUrlFallback
        }
        throw getFunctionArgumentsException(name, args)
      }
      "getYear" -> {
        return GetYear.withArgumentsValidation(args)
      }
      "index" -> {
        return StringIndex.withArgumentsValidation(args)
      }
      "lastIndex" -> {
        return StringLastIndex.withArgumentsValidation(args)
      }
      "len" -> {
        if (GetArrayLength.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArrayLength
        }
        if (StringLength.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringLength
        }
        if (GetArrayLength.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetArrayLength
        }
        if (StringLength.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringLength
        }
        throw getFunctionArgumentsException(name, args)
      }
      "max" -> {
        if (DoubleMax.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleMax
        }
        if (IntegerMax.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerMax
        }
        if (DoubleMax.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleMax
        }
        if (IntegerMax.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerMax
        }
        throw getFunctionArgumentsException(name, args)
      }
      "maxInteger" -> {
        return IntegerMaxValue.withArgumentsValidation(args)
      }
      "maxNumber" -> {
        return DoubleMaxValue.withArgumentsValidation(args)
      }
      "min" -> {
        if (DoubleMin.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleMin
        }
        if (IntegerMin.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerMin
        }
        if (DoubleMin.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleMin
        }
        if (IntegerMin.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerMin
        }
        throw getFunctionArgumentsException(name, args)
      }
      "minInteger" -> {
        return IntegerMinValue.withArgumentsValidation(args)
      }
      "minNumber" -> {
        return DoubleMinValue.withArgumentsValidation(args)
      }
      "mod" -> {
        if (DoubleMod.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleMod
        }
        if (IntegerMod.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerMod
        }
        if (DoubleMod.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleMod
        }
        if (IntegerMod.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerMod
        }
        throw getFunctionArgumentsException(name, args)
      }
      "mul" -> {
        if (DoubleMul.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleMul
        }
        if (IntegerMul.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerMul
        }
        if (DoubleMul.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleMul
        }
        if (IntegerMul.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerMul
        }
        throw getFunctionArgumentsException(name, args)
      }
      "nowLocal" -> {
        return NowLocal.withArgumentsValidation(args)
      }
      "padEnd" -> {
        if (PadEndInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return PadEndInteger
        }
        if (PadEndString.matchesArguments(args) == Function.MatchResult.Ok) {
          return PadEndString
        }
        if (PadEndInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return PadEndInteger
        }
        if (PadEndString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return PadEndString
        }
        throw getFunctionArgumentsException(name, args)
      }
      "padStart" -> {
        if (PadStartInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return PadStartInteger
        }
        if (PadStartString.matchesArguments(args) == Function.MatchResult.Ok) {
          return PadStartString
        }
        if (PadStartInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return PadStartInteger
        }
        if (PadStartString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return PadStartString
        }
        throw getFunctionArgumentsException(name, args)
      }
      "parseUnixTime" -> {
        return ParseUnixTime.withArgumentsValidation(args)
      }
      "parseUnixTimeAsLocal" -> {
        return ParseUnixTimeAsLocal.withArgumentsValidation(args)
      }
      "pi" -> {
        return Pi.withArgumentsValidation(args)
      }
      "replaceAll" -> {
        return StringReplaceAll.withArgumentsValidation(args)
      }
      "rgb" -> {
        return ColorRgb.withArgumentsValidation(args)
      }
      "round" -> {
        return DoubleRound.withArgumentsValidation(args)
      }
      "setColorAlpha" -> {
        if (ColorAlphaComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorAlphaComponentSetter
        }
        if (ColorStringAlphaComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringAlphaComponentSetter
        }
        if (ColorAlphaComponentSetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorAlphaComponentSetter
        }
        if (ColorStringAlphaComponentSetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringAlphaComponentSetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "setColorBlue" -> {
        if (ColorBlueComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorBlueComponentSetter
        }
        if (ColorStringBlueComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringBlueComponentSetter
        }
        if (ColorBlueComponentSetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorBlueComponentSetter
        }
        if (ColorStringBlueComponentSetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringBlueComponentSetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "setColorGreen" -> {
        if (ColorGreenComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorGreenComponentSetter
        }
        if (ColorStringGreenComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringGreenComponentSetter
        }
        if (ColorGreenComponentSetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorGreenComponentSetter
        }
        if (ColorStringGreenComponentSetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringGreenComponentSetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "setColorRed" -> {
        if (ColorRedComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorRedComponentSetter
        }
        if (ColorStringRedComponentSetter.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorStringRedComponentSetter
        }
        if (ColorRedComponentSetter.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorRedComponentSetter
        }
        if (ColorStringRedComponentSetter.matchesArgumentsWithCast(args) ==
            Function.MatchResult.Ok) {
          return ColorStringRedComponentSetter
        }
        throw getFunctionArgumentsException(name, args)
      }
      "setDay" -> {
        return SetDay.withArgumentsValidation(args)
      }
      "setHours" -> {
        return SetHours.withArgumentsValidation(args)
      }
      "setMillis" -> {
        return SetMillis.withArgumentsValidation(args)
      }
      "setMinutes" -> {
        return SetMinutes.withArgumentsValidation(args)
      }
      "setMonth" -> {
        return SetMonth.withArgumentsValidation(args)
      }
      "setSeconds" -> {
        return SetSeconds.withArgumentsValidation(args)
      }
      "setYear" -> {
        return SetYear.withArgumentsValidation(args)
      }
      "signum" -> {
        if (DoubleSignum.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleSignum
        }
        if (IntegerSignum.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerSignum
        }
        if (DoubleSignum.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleSignum
        }
        if (IntegerSignum.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerSignum
        }
        throw getFunctionArgumentsException(name, args)
      }
      "sin" -> {
        return Sine.withArgumentsValidation(args)
      }
      "sub" -> {
        if (DoubleSub.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleSub
        }
        if (IntegerSub.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerSub
        }
        if (DoubleSub.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleSub
        }
        if (IntegerSub.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerSub
        }
        throw getFunctionArgumentsException(name, args)
      }
      "substring" -> {
        return StringSubstring.withArgumentsValidation(args)
      }
      "sum" -> {
        if (DoubleSum.matchesArguments(args) == Function.MatchResult.Ok) {
          return DoubleSum
        }
        if (IntegerSum.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerSum
        }
        if (DoubleSum.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DoubleSum
        }
        if (IntegerSum.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerSum
        }
        throw getFunctionArgumentsException(name, args)
      }
      "testRegex" -> {
        return TestRegex.withArgumentsValidation(args)
      }
      "toBoolean" -> {
        if (IntegerToBoolean.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerToBoolean
        }
        if (StringToBoolean.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringToBoolean
        }
        if (IntegerToBoolean.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerToBoolean
        }
        if (StringToBoolean.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringToBoolean
        }
        throw getFunctionArgumentsException(name, args)
      }
      "toColor" -> {
        return StringToColor.withArgumentsValidation(args)
      }
      "toDegrees" -> {
        return RadiansToDegrees.withArgumentsValidation(args)
      }
      "toInteger" -> {
        if (BooleanToInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return BooleanToInteger
        }
        if (NumberToInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return NumberToInteger
        }
        if (StringToInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringToInteger
        }
        if (BooleanToInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return BooleanToInteger
        }
        if (NumberToInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return NumberToInteger
        }
        if (StringToInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringToInteger
        }
        throw getFunctionArgumentsException(name, args)
      }
      "toLowerCase" -> {
        return ToLowerCase.withArgumentsValidation(args)
      }
      "toNumber" -> {
        if (IntegerToNumber.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerToNumber
        }
        if (StringToNumber.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringToNumber
        }
        if (IntegerToNumber.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerToNumber
        }
        if (StringToNumber.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringToNumber
        }
        throw getFunctionArgumentsException(name, args)
      }
      "toRadians" -> {
        return DegreesToRadians.withArgumentsValidation(args)
      }
      "toString" -> {
        if (ArrayToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayToString
        }
        if (BooleanToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return BooleanToString
        }
        if (ColorToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorToString
        }
        if (DictToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return DictToString
        }
        if (IntegerToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerToString
        }
        if (NumberToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return NumberToString
        }
        if (StringToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringToString
        }
        if (UrlToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return UrlToString
        }
        if (ArrayToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayToString
        }
        if (BooleanToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return BooleanToString
        }
        if (ColorToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorToString
        }
        if (DictToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DictToString
        }
        if (IntegerToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerToString
        }
        if (NumberToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return NumberToString
        }
        if (StringToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringToString
        }
        if (UrlToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return UrlToString
        }
        throw getFunctionArgumentsException(name, args)
      }
      "toUpperCase" -> {
        return ToUpperCase.withArgumentsValidation(args)
      }
      "toUrl" -> {
        return StringToUrl.withArgumentsValidation(args)
      }
      "trim" -> {
        return Trim.withArgumentsValidation(args)
      }
      "trimLeft" -> {
        return TrimLeft.withArgumentsValidation(args)
      }
      "trimRight" -> {
        return TrimRight.withArgumentsValidation(args)
      }
    }
    throw EvaluableException("Unknown function name: $name.")
  }

  override fun getMethod(name: String, args: List<EvaluableType>): Function {
    when (name) {
      "containsKey" -> {
        return DictContainsKey.withArgumentsValidation(args)
      }
      "getArray" -> {
        if (ArrayGetArray.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetArray
        }
        if (GetArray.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetArray
        }
        if (ArrayGetArray.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetArray
        }
        if (GetArray.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetArray
        }
        throw getMethodArgumentsException(name, args)
      }
      "getBoolean" -> {
        if (ArrayGetBoolean.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetBoolean
        }
        if (GetBoolean.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetBoolean
        }
        if (ArrayGetBoolean.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetBoolean
        }
        if (GetBoolean.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetBoolean
        }
        throw getMethodArgumentsException(name, args)
      }
      "getColor" -> {
        if (ArrayGetColor.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetColor
        }
        if (GetColor.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetColor
        }
        if (ArrayGetColor.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetColor
        }
        if (GetColor.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetColor
        }
        throw getMethodArgumentsException(name, args)
      }
      "getDict" -> {
        if (ArrayGetDict.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetDict
        }
        if (GetDict.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetDict
        }
        if (ArrayGetDict.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetDict
        }
        if (GetDict.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetDict
        }
        throw getMethodArgumentsException(name, args)
      }
      "getInteger" -> {
        if (ArrayGetInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetInteger
        }
        if (GetInteger.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetInteger
        }
        if (ArrayGetInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetInteger
        }
        if (GetInteger.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetInteger
        }
        throw getMethodArgumentsException(name, args)
      }
      "getNumber" -> {
        if (ArrayGetNumber.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetNumber
        }
        if (GetNumber.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetNumber
        }
        if (ArrayGetNumber.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetNumber
        }
        if (GetNumber.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetNumber
        }
        throw getMethodArgumentsException(name, args)
      }
      "getString" -> {
        if (ArrayGetString.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetString
        }
        if (GetString.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetString
        }
        if (ArrayGetString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetString
        }
        if (GetString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetString
        }
        throw getMethodArgumentsException(name, args)
      }
      "getUrl" -> {
        if (ArrayGetUrl.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayGetUrl
        }
        if (GetUrl.matchesArguments(args) == Function.MatchResult.Ok) {
          return GetUrl
        }
        if (ArrayGetUrl.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayGetUrl
        }
        if (GetUrl.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return GetUrl
        }
        throw getMethodArgumentsException(name, args)
      }
      "isEmpty" -> {
        if (ArrayIsEmpty.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayIsEmpty
        }
        if (DictIsEmpty.matchesArguments(args) == Function.MatchResult.Ok) {
          return DictIsEmpty
        }
        if (ArrayIsEmpty.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayIsEmpty
        }
        if (DictIsEmpty.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DictIsEmpty
        }
        throw getMethodArgumentsException(name, args)
      }
      "toString" -> {
        if (ArrayToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return ArrayToString
        }
        if (BooleanToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return BooleanToString
        }
        if (ColorToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return ColorToString
        }
        if (DictToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return DictToString
        }
        if (IntegerToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return IntegerToString
        }
        if (NumberToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return NumberToString
        }
        if (StringToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return StringToString
        }
        if (UrlToString.matchesArguments(args) == Function.MatchResult.Ok) {
          return UrlToString
        }
        if (ArrayToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ArrayToString
        }
        if (BooleanToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return BooleanToString
        }
        if (ColorToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return ColorToString
        }
        if (DictToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return DictToString
        }
        if (IntegerToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return IntegerToString
        }
        if (NumberToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return NumberToString
        }
        if (StringToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return StringToString
        }
        if (UrlToString.matchesArgumentsWithCast(args) == Function.MatchResult.Ok) {
          return UrlToString
        }
        throw getMethodArgumentsException(name, args)
      }
    }
    throw EvaluableException("Unknown method name: $name.")
  }

  public fun warmUp() {
    AddMillis
    ArrayToString
    BooleanToInteger
    BooleanToString
    ColorAlphaComponentGetter
    ColorAlphaComponentSetter
    ColorArgb
    ColorBlueComponentGetter
    ColorBlueComponentSetter
    ColorGreenComponentGetter
    ColorGreenComponentSetter
    ColorRedComponentGetter
    ColorRedComponentSetter
    ColorRgb
    ColorStringAlphaComponentGetter
    ColorStringAlphaComponentSetter
    ColorStringBlueComponentGetter
    ColorStringBlueComponentSetter
    ColorStringGreenComponentGetter
    ColorStringGreenComponentSetter
    ColorStringRedComponentGetter
    ColorStringRedComponentSetter
    ColorToString
    DegreesToRadians
    DictToString
    DoubleAbs
    DoubleCeil
    DoubleCopySign
    DoubleDiv
    DoubleFloor
    DoubleMax
    DoubleMaxValue
    DoubleMin
    DoubleMinValue
    DoubleMod
    DoubleMul
    DoubleRound
    DoubleSignum
    DoubleSub
    DoubleSum
    EncodeRegex
    FormatDateAsLocal
    FormatDateAsLocalWithLocale
    FormatDateAsUTC
    FormatDateAsUTCWithLocale
    GetArrayBoolean
    GetArrayColor
    GetArrayFromArray
    GetArrayFromDict
    GetArrayInteger
    GetArrayLength
    GetArrayNumber
    GetArrayOptBoolean
    GetArrayOptColorWithColorFallback
    GetArrayOptColorWithStringFallback
    GetArrayOptInteger
    GetArrayOptNumber
    GetArrayOptString
    GetArrayOptUrlWithStringFallback
    GetArrayOptUrlWithUrlFallback
    GetArrayString
    GetArrayUrl
    GetBooleanFromArray
    GetBooleanFromDict
    GetBooleanValue
    GetColorFromArray
    GetColorFromDict
    GetColorValue
    GetColorValueString
    GetDay
    GetDayOfWeek
    GetDictBoolean
    GetDictColor
    GetDictFromArray
    GetDictFromDict
    GetDictInteger
    GetDictNumber
    GetDictOptBoolean
    GetDictOptColorWithColorFallback
    GetDictOptColorWithStringFallback
    GetDictOptInteger
    GetDictOptNumber
    GetDictOptString
    GetDictOptUrlWithStringFallback
    GetDictOptUrlWithUrlFallback
    GetDictString
    GetDictUrl
    GetHours
    GetIntegerFromArray
    GetIntegerFromDict
    GetIntegerValue
    GetIntervalHours
    GetIntervalMinutes
    GetIntervalSeconds
    GetIntervalTotalDays
    GetIntervalTotalHours
    GetIntervalTotalMinutes
    GetIntervalTotalSeconds
    GetIntervalTotalWeeks
    GetMillis
    GetMinutes
    GetMonth
    GetNumberFromArray
    GetNumberFromDict
    GetNumberValue
    GetOptArrayFromArray
    GetOptArrayFromDict
    GetOptBooleanFromArray
    GetOptBooleanFromDict
    GetOptColorFromArrayWithColorFallback
    GetOptColorFromArrayWithStringFallback
    GetOptColorFromDictWithColorFallback
    GetOptColorFromDictWithStringFallback
    GetOptDictFromArray
    GetOptDictFromDict
    GetOptIntegerFromArray
    GetOptIntegerFromDict
    GetOptNumberFromArray
    GetOptNumberFromDict
    GetOptStringFromArray
    GetOptStringFromDict
    GetOptUrlFromArrayWithStringFallback
    GetOptUrlFromArrayWithUrlFallback
    GetOptUrlFromDictWithStringFallback
    GetOptUrlFromDictWithUrlFallback
    GetSeconds
    GetStoredArrayValue
    GetStoredBooleanValue
    GetStoredColorValue
    GetStoredColorValueString
    GetStoredDictValue
    GetStoredIntegerValue
    GetStoredNumberValue
    GetStoredStringValue
    GetStoredUrlValueWithStringFallback
    GetStoredUrlValueWithUrlFallback
    GetStringFromArray
    GetStringFromDict
    GetStringValue
    GetUrlFromArray
    GetUrlFromDict
    GetUrlValueWithStringFallback
    GetUrlValueWithUrlFallback
    GetYear
    IntegerAbs
    IntegerCopySign
    IntegerDiv
    IntegerMax
    IntegerMaxValue
    IntegerMin
    IntegerMinValue
    IntegerMod
    IntegerMul
    IntegerSignum
    IntegerSub
    IntegerSum
    IntegerToBoolean
    IntegerToNumber
    IntegerToString
    NowLocal
    NumberToInteger
    NumberToString
    PadEndInteger
    PadEndString
    PadStartInteger
    PadStartString
    ParseUnixTime
    ParseUnixTimeAsLocal
    Pi
    RadiansToDegrees
    SetDay
    SetHours
    SetMillis
    SetMinutes
    SetMonth
    SetSeconds
    SetYear
    Sine
    StringContains
    StringDecodeUri
    StringEncodeUri
    StringIndex
    StringLastIndex
    StringLength
    StringReplaceAll
    StringSubstring
    StringToBoolean
    StringToColor
    StringToInteger
    StringToNumber
    StringToString
    StringToUrl
    TestRegex
    ToLowerCase
    ToUpperCase
    Trim
    TrimLeft
    TrimRight
    UrlToString
  }
}
