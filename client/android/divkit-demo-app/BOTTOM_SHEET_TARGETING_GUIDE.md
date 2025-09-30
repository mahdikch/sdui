# Bottom Sheet Targeting System

## Overview

The new Bottom Sheet Targeting System allows you to manage multiple bottom sheets and set variables on specific bottom sheets from any other bottom sheet or the main screen.

## Key Features

1. **Multiple Bottom Sheet Management**: Track and manage multiple bottom sheet instances
2. **Targeting Capabilities**: Set variables on specific bottom sheets using various targeting methods
3. **Backward Compatibility**: Existing code continues to work without changes

## How It Works

### 1. BottomSheetManager

The `BottomSheetManager` class handles all bottom sheet instances:
- Maintains a stack of bottom sheets (first = oldest, last = newest)
- Assigns unique IDs to each bottom sheet
- Provides targeting methods for variable setting

### 2. Targeting Methods

You can target bottom sheets using:

- **`current`** or **`top`** or **`latest`**: Targets the most recently opened bottom sheet
- **`first`** or **`bottom`** or **`oldest`**: Targets the first (oldest) bottom sheet
- **ID**: Targets by unique ID (e.g., "bottomsheet_1234567890_abc12345")
- **Position**: Targets by position in stack (0-based, e.g., "0" for first, "1" for second)

## Usage Examples

### Method 1: Server Response with Targeting

From any bottom sheet, send a server request that includes targeting information:

```json
{
  "bottom_sheet_variable_first_variableName": "newValue",
  "bottom_sheet_variable_current_anotherVariable": "anotherValue"
}
```

**Key Format**: `bottom_sheet_variable_[target]_[variableName]`

**Target Options**:
- `first`: First bottom sheet
- `current`: Current bottom sheet  
- `0`, `1`, `2`, etc.: Position in stack (0-based)
- `bottomsheet_1234567890_abc12345`: Specific ID

### Method 2: Direct Action with Targeting

Use the new action authority `set_variable_to_bottomsheet`:

```json
{
  "action": "div-action://set_variable_to_bottomsheet?name=variableName&value=newValue&target=first"
}
```

**Parameters**:
- `name`: Variable name
- `value`: Variable value
- `target`: Target identifier (first, current, position, or ID)

## Implementation Details

### BottomSheetManager API

```kotlin
class BottomSheetManager {
    // Add a new bottom sheet
    fun addBottomSheet(bottomSheet: BottomSheetDiv, id: String? = null): String
    
    // Remove a bottom sheet
    fun removeBottomSheet(bottomSheet: BottomSheetDiv)
    
    // Get current (topmost) bottom sheet
    fun getCurrentBottomSheet(): BottomSheetDiv?
    
    // Get first (bottommost) bottom sheet
    fun getFirstBottomSheet(): BottomSheetDiv?
    
    // Get bottom sheet by ID
    fun getBottomSheetById(id: String): BottomSheetDiv?
    
    // Get bottom sheet by position
    fun getBottomSheetByPosition(position: Int): BottomSheetDiv?
    
    // Set variable on target
    fun setVariableOnTarget(target: String, key: String, value: String): Boolean
    
    // Get count of active bottom sheets
    fun getBottomSheetCount(): Int
}
```

### Variable Setting Logic

The system automatically parses targeting information from server responses:

```kotlin
// Format: bottom_sheet_variable_[target]_[variableName]
val parts = key.split("_")
if (parts.size >= 4) {
    val target = parts[2] // target (current, first, id, position)
    val variableName = parts.drop(3).joinToString("_") // variable name
    bottomSheetManager.setVariableOnTarget(target, variableName, value)
}
```

## Example Scenarios

### Scenario 1: Setting Variable on First Bottom Sheet from Second Bottom Sheet

1. **First Bottom Sheet** opens with ID: `bottomsheet_1234567890_abc12345`
2. **Second Bottom Sheet** opens with ID: `bottomsheet_1234567891_def67890`
3. From **Second Bottom Sheet**, send server request:

```json
{
  "bottom_sheet_variable_first_userSelection": "selectedOption",
  "bottom_sheet_variable_0_formData": "formValue"
}
```

### Scenario 2: Using Direct Action

From any bottom sheet, trigger action:

```json
{
  "action": "div-action://set_variable_to_bottomsheet?name=selectedItem&value=item123&target=first"
}
```

### Scenario 3: Targeting by Specific ID

If you know the exact ID of a bottom sheet:

```json
{
  "bottom_sheet_variable_bottomsheet_1234567890_abc12345_status": "completed"
}
```

## Migration from Old System

The new system is **backward compatible**. Existing code using:

```kotlin
// Old way - still works
if (key.contains("bottom_sheet_variable"))
    btmSheet.setVariableOnBottomSheet(key, value)
```

Will now fallback to setting variables on the current bottom sheet if targeting information is not provided.

## Best Practices

1. **Use descriptive targeting**: Prefer `first` or `current` over position numbers for clarity
2. **Handle errors gracefully**: The system provides fallbacks if targeting fails
3. **Use unique IDs**: For complex scenarios with many bottom sheets, use unique IDs
4. **Test thoroughly**: Verify that variables are set on the correct bottom sheets

## Debugging

To debug bottom sheet targeting:

1. Check `bottomSheetManager.getBottomSheetCount()` to see how many bottom sheets are active
2. Use `bottomSheetManager.getAllBottomSheetIds()` to see all available IDs
3. Verify the targeting string format matches expected patterns
4. Check logs for targeting success/failure messages

## Limitations

1. **Stack-based targeting**: Position-based targeting (0, 1, 2) depends on the order bottom sheets were opened
2. **ID persistence**: Bottom sheet IDs are generated automatically and may not persist across app restarts
3. **Memory management**: Bottom sheets are automatically removed from the manager when dismissed, but manual cleanup may be needed in some cases
