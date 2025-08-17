# Custom Props Variable Substitution Fix

## Problem Description

When using variable expressions like `@{secounds}` in the `custom_props` of a custom component in DivKit Android, the variable substitution was not working properly. Instead of evaluating the variable and returning its actual value (e.g., `10`), the system was returning the literal string `"@{secounds}"`.

## Root Cause

The issue was in the `DemoCustomContainerAdapter.kt` file where `custom_props` values were being accessed directly from the `JSONObject` without using the `ExpressionResolver` to evaluate variable expressions.

**Before (Problematic Code):**
```kotlin
if (div.customType == "timer_button") {
    seconds = div.customProps?.get("seconds") as Int  // Returns "@{secounds}" as string
    timerButtonNext = div.customProps?.get("next_page") as String
}
```

## Solution

The fix involves creating helper functions that properly evaluate variable expressions using the `ExpressionResolver`:

**After (Fixed Code):**
```kotlin
if (div.customType == "timer_button") {
    seconds = evaluateCustomProp(div, "seconds", expressionResolver, 0)
    timerButtonNext = evaluateCustomPropString(div, "next_page", expressionResolver, "")
}
```

### Helper Functions Added

1. **`evaluateCustomProp()`** - For integer/numeric values
2. **`evaluateCustomPropString()`** - For string values

These functions:
- Check if the value is a string that starts with `@{` and ends with `}`
- Extract the variable name from the expression
- Use the `ExpressionResolver` to evaluate the variable
- Return the evaluated value or a default value if evaluation fails

## Example JSON

```json
{
  "type": "custom",
  "custom_type": "timer_button",
  "custom_props": {
    "seconds": "@{secounds}",
    "next_page": "op/guide"
  }
}
```

With the variable defined as:
```json
{
  "type": "number",
  "name": "secounds",
  "value": 10
}
```

## Result

- **Before fix**: `seconds` would be `"@{secounds}"` (string)
- **After fix**: `seconds` will be `10` (integer)

## Files Modified

- `client/android/divkit-demo-app/src/main/java/com/yandex/divkit/demo/div/DemoCustomContainerAdapter.kt`

## Testing

To test the fix:

1. Create a custom component with variable expressions in `custom_props`
2. Define the variables in the card's variables section
3. Verify that the custom component receives the evaluated values instead of the literal expressions

## Impact

This fix ensures that custom components can properly use variable expressions in their `custom_props`, making them more dynamic and configurable through the DivKit variable system. 