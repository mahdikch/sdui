# Quick Start: Dynamic Item Addition

## 🚀 **3-Step Setup**

### **Step 1: Store Template in Database**

```kotlin
// Key: "my_item_template"
val template = """
{
  "type": "container",
  "id": "item_@{item_id}",
  "items": [
    {"type": "text", "text": "@{item_name}"},
    {"type": "text", "text": "@{item_family}"},
    {"type": "text", "text": "@{item_age}"}
  ],
  "border": {"stroke": {"color": "#dedede", "width": 1}},
  "paddings": {"top": 12, "bottom": 12, "left": 16, "right": 16},
  "margins": {"top": 8}
}
"""

mehdiViewModel.insertItemToDb(
    PhPlusDB(null, "my_item_template", template)
)
```

### **Step 2: Add Container to Your Page**

```json
{
  "type": "container",
  "id": "items_list",
  "items": [],
  "orientation": "vertical"
}
```

### **Step 3: Add Action to Submit Button**

```json
{
  "actions": [{
    "url": "div-action://add_item_from_template?template=my_item_template&container_id=items_list&item_name=@{input_name}&item_family=@{input_family}&item_age=@{input_age}&item_id=@{input_name}"
  }]
}
```

## ✅ **Done!**

Click submit → Item added!
Click again → Another item added!

## 📖 **Action Format**

```
div-action://add_item_from_template?
  template=[templateKey]              ← Required: Template key in DB
  &container_id=[containerId]         ← Required: Container ID
  &[var1]=[value1]                    ← Optional: Template variables
  &[var2]=[value2]                    ← Optional: More variables
  &position=[number]                  ← Optional: Insert position
```

## 💡 **Tips**

1. **Template variables** (e.g., `@{item_name}`) must match **action parameters** (e.g., `item_name=value`)
2. Use `@{variable}` in action to get values from form inputs
3. Omit `position` to append to end, use `position=0` to insert at beginning
4. Each item gets a unique ID automatically

## 🎯 **Your Punishment Form Example**

**Action:**
```
div-action://add_item_from_template?template=punishment_item_template&container_id=punishments_list&item_name=@{name}&item_lastname=@{lastname}&item_daraje=@{daraje}&item_yegan=@{yegan}&item_id=@{personal_code}
```

**Template Variables:**
- `@{item_name}` → Gets value from `name` variable
- `@{item_lastname}` → Gets value from `lastname` variable
- `@{item_daraje}` → Gets value from `daraje` variable
- `@{item_yegan}` → Gets value from `yegan` variable
- `@{item_id}` → Gets value from `personal_code` variable

## 🎉 **Result**

Each time you click submit, a new punishment record is added to the list with all the form data!

