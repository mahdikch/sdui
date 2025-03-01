import Foundation
import VGSL

extension [String: Function] {
  mutating func addArrayFunctions() {
    addFunction("getArrayFromArray", _getArray)
    addFunction("getOptArrayFromArray", _getOptArray)

    addFunction("getDictFromArray", _getDict)
    addFunction("getOptDictFromArray", _getOptDict)

    addFunctions("Boolean", _getBoolean)
    addFunctions("OptBoolean", _getOptBoolean)

    addFunctions("Color", _getColor)
    addFunctions("OptColor", _getOptColor)

    addFunctions("Integer", _getInteger)
    addFunctions("OptInteger", _getOptInteger)

    addFunctions("Number", _getNumber)
    addFunctions("OptNumber", _getOptNumber)

    addFunctions("String", _getString)
    addFunctions("OptString", _getOptString)

    addFunctions("Url", _getUrl)
    addFunctions("OptUrl", _getOptUrl)

    addFunction("len", FunctionUnary<DivArray, Int> { $0.count })
  }

  mutating func addArrayMethods() {
    addFunction("getArray", _getArray)
    addFunction("getBoolean", _getBoolean)
    addFunction("getColor", _getColor)
    addFunction("getDict", _getDict)
    addFunction("getInteger", _getInteger)
    addFunction("getNumber", _getNumber)
    addFunction("getString", _getString)
    addFunction("getUrl", _getUrl)
    addFunction("isEmpty", _isEmpty)
  }

  private mutating func addFunctions(
    _ typeName: String,
    _ function: Function
  ) {
    self["get\(typeName)FromArray"] = function
    self["getArray\(typeName)"] = function
  }
}

private let _getArray = FunctionBinary<DivArray, Int, DivArray> {
  try $0.getArray(index: $1)
}

private let _getBoolean = FunctionBinary<DivArray, Int, Bool> {
  try $0.getBoolean(index: $1)
}

private let _getColor = FunctionBinary<DivArray, Int, Color> {
  try $0.getColor(index: $1)
}

private let _getDict = FunctionBinary<DivArray, Int, DivDictionary> {
  try $0.getDict(index: $1)
}

private let _getInteger = FunctionBinary<DivArray, Int, Int> {
  try $0.getInteger(index: $1)
}

private let _getNumber = FunctionBinary<DivArray, Int, Double> {
  try $0.getNumber(index: $1)
}

private let _getString = FunctionBinary<DivArray, Int, String> {
  try $0.getString(index: $1)
}

private let _getUrl = FunctionBinary<DivArray, Int, URL> {
  try $0.getUrl(index: $1)
}

private let _isEmpty = FunctionUnary<DivArray, Bool> {
  $0.isEmpty
}

private let _getOptArray = FunctionBinary<DivArray, Int, DivArray> {
  (try? $0.getArray(index: $1)) ?? []
}

private let _getOptBoolean = FunctionTernary<DivArray, Int, Bool, Bool> {
  (try? $0.getBoolean(index: $1)) ?? $2
}

private let _getOptColor = OverloadedFunction(functions: [
  FunctionTernary<DivArray, Int, Color, Color> {
    (try? $0.getColor(index: $1)) ?? $2
  },
  FunctionTernary<DivArray, Int, String, Color> {
    if let value = try? $0.getColor(index: $1) {
      return value
    }
    return Color.color(withHexString: $2)!
  },
])

private let _getOptDict = FunctionBinary<DivArray, Int, DivDictionary> {
  (try? $0.getDict(index: $1)) ?? [:]
}

private let _getOptInteger = FunctionTernary<DivArray, Int, Int, Int> {
  (try? $0.getInteger(index: $1)) ?? $2
}

private let _getOptNumber = FunctionTernary<DivArray, Int, Double, Double> {
  (try? $0.getNumber(index: $1)) ?? $2
}

private let _getOptString = FunctionTernary<DivArray, Int, String, String> {
  (try? $0.getString(index: $1)) ?? $2
}

private let _getOptUrl = OverloadedFunction(functions: [
  FunctionTernary<DivArray, Int, URL, URL> {
    (try? $0.getUrl(index: $1)) ?? $2
  },
  FunctionTernary<DivArray, Int, String, URL> {
    if let value = try? $0.getUrl(index: $1) {
      return value
    }
    return URL(string: $2)!
  },
])

extension DivArray {
  fileprivate func getArray(index: Int) throws -> DivArray {
    let value = try getValue(index: index)
    guard let arrayValue = value as? DivArray else {
      throw ExpressionError.incorrectType("Array", value)
    }
    return arrayValue
  }

  fileprivate func getDict(index: Int) throws -> DivDictionary {
    let value = try getValue(index: index)
    guard let dictValue = value as? DivDictionary else {
      throw ExpressionError.incorrectType("Dict", value)
    }
    return dictValue
  }

  fileprivate func getBoolean(index: Int) throws -> Bool {
    let value = try getValue(index: index)
    guard value.isBool, let boolValue = value as? Bool else {
      throw ExpressionError.incorrectType("Boolean", value)
    }
    return boolValue
  }

  fileprivate func getColor(index: Int) throws -> Color {
    let value = try getValue(index: index)
    guard let stringValue = value as? String else {
      throw ExpressionError.incorrectType("Color", value)
    }
    guard let color = Color.color(withHexString: stringValue) else {
      throw ExpressionError("Unable to convert value to Color, expected format #AARRGGBB.")
    }
    return color
  }

  fileprivate func getInteger(index: Int) throws -> Int {
    let value = try getValue(index: index)
    if value.isBool {
      throw ExpressionError.incorrectType("Integer", value)
    }
    guard let intValue = value as? Int else {
      if let doubleValue = value as? Double {
        if doubleValue < Double(Int.min) || doubleValue > Double(Int.max) {
          throw ExpressionError.integerOverflow()
        }
        throw ExpressionError("Cannot convert value to integer.")
      }
      throw ExpressionError.incorrectType("Integer", value)
    }
    return intValue
  }

  fileprivate func getNumber(index: Int) throws -> Double {
    let value = try getValue(index: index)
    if value.isBool {
      throw ExpressionError.incorrectType("Number", value)
    }
    if let numberValue = value as? Double {
      return numberValue
    }
    if let intValue = value as? Int {
      return Double(intValue)
    }
    throw ExpressionError.incorrectType("Number", value)
  }

  fileprivate func getString(index: Int) throws -> String {
    let value = try getValue(index: index)
    guard let stringValue = value as? String else {
      throw ExpressionError.incorrectType("String", value)
    }
    return stringValue
  }

  fileprivate func getUrl(index: Int) throws -> URL {
    let value = try getValue(index: index)
    guard
      let stringValue = value as? String,
      let url = URL(string: stringValue)
    else {
      throw ExpressionError.incorrectType("Url", value)
    }
    return url
  }

  private func getValue(index: Int) throws -> AnyHashable {
    if index >= 0, index < count {
      return self[index]
    }
    throw ExpressionError("Requested index (\(index)) out of bounds array size (\(count)).")
  }
}
