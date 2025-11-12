# Bottom Sheet Variable Key Format Guide

## Overview

When setting variables on bottom sheets from server responses, you need to use the correct key format.

## Key Formats

### Format 1: Set Variable on Current Bottom Sheet (Default)

```
bottom_sheet_variable_[variableName]
```

**Examples:**
```json
{
  "bottom_sheet_variable_name": "اكبر",
  "bottom_sheet_variable_lastname": "حيدري زنگلاني",
  "bottom_sheet_variable_daraje": "ستوان يکم",
  "bottom_sheet_variable_yegan": "فرماندهي انتظامي استان البرز"
}
```

This will set the variables on the **current (topmost)** bottom sheet.

### Format 2: Set Variable on Specific Target Bottom Sheet

```
bottom_sheet_variable_[target]_[variableName]
```

**Valid Targets:**
- `current`, `top`, `latest` - Current (topmost) bottom sheet
- `first`, `bottom`, `oldest` - First (bottommost) bottom sheet
- `0`, `1`, `2`, etc. - Position in stack (0-based)
- Unique ID - Specific bottom sheet ID (e.g., `bottomsheet_1760010461541_909ec510`)

**Examples:**
```json
{
  "bottom_sheet_variable_first_name": "اكبر",
  "bottom_sheet_variable_0_lastname": "حيدري زنگلاني",
  "bottom_sheet_variable_1_daraje": "ستوان يکم",
  "bottom_sheet_variable_current_yegan": "فرماندهي انتظامي استان البرز"
}
```

## Common Mistakes

### ❌ Wrong Format
```json
{
  "bottom_sheet_variable_name": "value"
}
```
When split by `_`, this becomes:
- parts = ["bottom", "sheet", "variable", "name"]
- target = "variable" ❌ (Invalid target!)
- variableName = "name"

### ✅ Correct Format (No Target)
```json
{
  "bottom_sheet_variable_name": "value"
}
```
After removing prefix `"bottom_sheet_variable_"`:
- afterPrefix = "name"
- No target specified → defaults to "current"
- variableName = "name" ✅

### ✅ Correct Format (With Target)
```json
{
  "bottom_sheet_variable_first_name": "value"
}
```
After removing prefix `"bottom_sheet_variable_"`:
- afterPrefix = "first_name"
- parts = ["first", "name"]
- target = "first" ✅
- variableName = "name" ✅

## Your Current Scenario

Based on your logs, you're using:
```json
{
  "bottom_sheet_variable_name": "اكبر",
  "bottom_sheet_variable_daraje": "ستوان يکم",
  "bottom_sheet_variable_lastname": "حيدري زنگلاني",
  "bottom_sheet_variable_yegan": "فرماندهي انتظامي استان البرز"
}
```

**This will now work correctly!** The new parsing logic will:
1. Remove the `"bottom_sheet_variable_"` prefix
2. Detect that there's no explicit target
3. Default to `"current"` target
4. Set the variables on the current bottom sheet

## Expected Logs After Fix

You should now see:
```
MehdiActivity: Processing bottom_sheet_variable key=bottom_sheet_variable_name
MehdiActivity: After prefix removal: name, parts=[name]
MehdiActivity: No target specified, using current - variableName=name, value=اكبر
BottomSheetManager: target=current, key=name, value=اكبر
BottomSheetManager: Targeting current (latest) bottom sheet
BottomSheetDiv: Setting variable - key=name, value=اكبر
BottomSheetDiv: Variable set successfully!
MehdiActivity: Variable setting success=true
```

## Best Practices

1. **For current bottom sheet**: Use `bottom_sheet_variable_[variableName]`
2. **For specific target**: Use `bottom_sheet_variable_[target]_[variableName]`
3. **Variable names with underscores**: Supported (e.g., `bottom_sheet_variable_first_user_name`)
4. **Multi-word targets**: Not supported (use single-word targets like "first", "current", or numbers)

## Testing

After this fix, your variables should appear correctly in the bottom sheet. The system will:
1. Parse the key correctly
2. Identify the target (or default to "current")
3. Find the correct bottom sheet
4. Set the variable
5. The UI will update to show the new values
