# Dynamic Item Addition Guide - Offline Patch Generation

## Overview

This system allows you to dynamically add items to containers using templates stored in the database. The patch generation happens **offline** on the client side, without requiring server-side patch generation.

## How It Works

1. **Store template** in database with a unique key
2. **Trigger action** with template key, container ID, and variable values
3. **System generates patch** offline using the template
4. **Patch is applied** to add the item to the container

## Complete Example: Form with Dynamic Item List

### Step 1: Create the Template

Store this template in your database with key: `"punishment_item_template"`

```json
{
  "type": "container",
  "id": "item_@{item_id}",
  "items": [
    {
      "type": "container",
      "items": [
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "text": "@{item_name}",
              "font_size": 14,
              "font_weight": "bold",
              "text_color": "#3b3b3b",
              "text_alignment_horizontal": "right"
            },
            {
              "type": "text",
              "text": "نام",
              "font_size": 12,
              "text_color": "#777676",
              "text_alignment_horizontal": "right"
            }
          ],
          "orientation": "horizontal",
          "content_alignment_horizontal": "space-between"
        },
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "text": "@{item_family}",
              "font_size": 14,
              "font_weight": "bold",
              "text_color": "#3b3b3b",
              "text_alignment_horizontal": "right"
            },
            {
              "type": "text",
              "text": "نام خانوادگی",
              "font_size": 12,
              "text_color": "#777676",
              "text_alignment_horizontal": "right"
            }
          ],
          "orientation": "horizontal",
          "content_alignment_horizontal": "space-between",
          "margins": {"top": 8}
        },
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "text": "@{item_age}",
              "font_size": 14,
              "font_weight": "bold",
              "text_color": "#3b3b3b",
              "text_alignment_horizontal": "right"
            },
            {
              "type": "text",
              "text": "سن",
              "font_size": 12,
              "text_color": "#777676",
              "text_alignment_horizontal": "right"
            }
          ],
          "orientation": "horizontal",
          "content_alignment_horizontal": "space-between",
          "margins": {"top": 8}
        }
      ],
      "orientation": "vertical"
    }
  ],
  "border": {
    "stroke": {"color": "#dedede", "width": 1},
    "corners_radius": 6
  },
  "paddings": {"top": 12, "bottom": 12, "left": 16, "right": 16},
  "margins": {"top": 8},
  "background": [{"type": "solid", "color": "#f9f9f9"}]
}
```

### Step 2: Create Your Page with Form

```json
{
  "card": {
    "log_id": "form_page",
    "states": [{
      "state_id": 0,
      "div": {
        "type": "container",
        "orientation": "vertical",
        "items": [
          {
            "type": "text",
            "text": "اطلاعات فرد",
            "font_size": 16,
            "font_weight": "bold",
            "text_color": "#3b3b3b"
          },
          {
            "type": "input",
            "text_variable": "input_name",
            "hint_text": "نام",
            "margins": {"top": 16}
          },
          {
            "type": "input",
            "text_variable": "input_family",
            "hint_text": "نام خانوادگی",
            "margins": {"top": 8}
          },
          {
            "type": "input",
            "text_variable": "input_age",
            "hint_text": "سن",
            "margins": {"top": 8}
          },
          {
            "type": "container",
            "actions": [{
              "log_id": "submit_form",
              "url": "div-action://add_item_from_template?template=punishment_item_template&container_id=items_container&item_name=@{input_name}&item_family=@{input_family}&item_age=@{input_age}&item_id=@{input_name}_@{input_family}"
            }],
            "items": [
              {
                "type": "text",
                "text": "ثبت",
                "font_size": 14,
                "text_color": "#ffffff"
              }
            ],
            "background": [{"type": "solid", "color": "#193d8d"}],
            "border": {"corners_radius": 6},
            "height": {"type": "fixed", "value": 48},
            "content_alignment_horizontal": "center",
            "content_alignment_vertical": "center",
            "margins": {"top": 16}
          },
          {
            "type": "text",
            "text": "لیست افراد ثبت شده",
            "font_size": 14,
            "font_weight": "bold",
            "text_color": "#3b3b3b",
            "margins": {"top": 32}
          },
          {
            "type": "container",
            "id": "items_container",
            "items": [],
            "orientation": "vertical",
            "margins": {"top": 8}
          }
        ],
        "paddings": {"top": 24, "bottom": 24, "left": 24, "right": 24}
      }
    }],
    "variables": [
      {
        "type": "string",
        "name": "input_name",
        "value": ""
      },
      {
        "type": "string",
        "name": "input_family",
        "value": ""
      },
      {
        "type": "string",
        "name": "input_age",
        "value": ""
      }
    ]
  }
}
```

## 🎯 **Action Format**

```
div-action://add_item_from_template?template=[templateKey]&container_id=[containerId]&[variable1]=[value1]&[variable2]=[value2]...
```

### **Required Parameters:**
- `template`: Key of the template in database
- `container_id`: ID of the container to add items to

### **Optional Parameters:**
- `position`: Position to insert (0 = beginning, null = end)
- Any other parameters become variables for the template

### **Example Actions:**

**Basic (Append to End):**
```
div-action://add_item_from_template?template=punishment_item_template&container_id=items_container&item_name=اكبر&item_family=حيدري&item_age=25
```

**Insert at Beginning:**
```
div-action://add_item_from_template?template=punishment_item_template&container_id=items_container&position=0&item_name=اكبر&item_family=حيدري&item_age=25
```

**With Dynamic Values from Form:**
```
div-action://add_item_from_template?template=punishment_item_template&container_id=items_container&item_name=@{input_name}&item_family=@{input_family}&item_age=@{input_age}
```

## 📝 **Variable Replacement**

The system automatically replaces all `@{variable_name}` in the template with the values you provide in the action URL.

**Template:**
```json
{
  "type": "text",
  "text": "@{item_name}"
}
```

**Action:**
```
?item_name=اكبر
```

**Result:**
```json
{
  "type": "text",
  "text": "اكبر"
}
```

## 🔄 **Complete Flow**

### **1. User Fills Form**
- Name: "اكبر"
- Family: "حيدري زنگلاني"
- Age: "25"

### **2. User Clicks Submit**
Action triggered:
```
div-action://add_item_from_template?template=punishment_item_template&container_id=items_container&item_name=@{input_name}&item_family=@{input_family}&item_age=@{input_age}
```

### **3. System Processes**
1. Loads template from database: `punishment_item_template`
2. Extracts variables: `{item_name: "اكبر", item_family: "حيدري زنگلاني", item_age: "25"}`
3. Replaces `@{item_name}` → "اكبر", `@{item_family}` → "حيدري زنگلاني", etc.
4. Generates patch with the processed item
5. Applies patch to add item to `items_container`

### **4. Result**
New item appears below the form with the submitted data!

### **5. Submit Again**
Each submit adds another item to the list.

## 🎨 **Advanced Features**

### **Unique Item IDs**

Use dynamic IDs to identify items:
```json
{
  "id": "item_@{item_id}"
}
```

Action:
```
?item_id=@{input_name}_@{input_family}
```

Result:
```json
{
  "id": "item_اكبر_حيدري"
}
```

### **Conditional Styling**

Template can include conditional logic:
```json
{
  "type": "text",
  "text": "@{item_status}",
  "text_color": "@{item_status == 'active' ? '#00ff00' : '#ff0000'}"
}
```

### **Nested Variables**

Support complex structures:
```json
{
  "type": "container",
  "items": [
    {"type": "text", "text": "@{item_title}"},
    {"type": "text", "text": "@{item_description}"},
    {"type": "text", "text": "@{item_date}"}
  ]
}
```

## 🛠️ **Database Setup**

Store your templates in the database:

```kotlin
// In your initialization or server response:
mehdiViewModel.insertItemToDb(
    PhPlusDB(
        null,
        "punishment_item_template",
        templateJsonString
    )
)
```

## 📊 **Real-World Example: Your Punishment Form**

### **Template in Database: `"punishment_item_template"`**
```json
{
  "type": "container",
  "id": "punishment_@{item_id}",
  "items": [
    {
      "type": "container",
      "items": [
        {
          "type": "text",
          "text": "@{item_name}",
          "font_size": 12,
          "font_weight": "bold",
          "text_color": "#2c2c2c"
        },
        {
          "type": "text",
          "text": "نام",
          "font_size": 12,
          "text_color": "#7f7f7f"
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "space-between"
    },
    {
      "type": "container",
      "items": [
        {
          "type": "text",
          "text": "@{item_lastname}",
          "font_size": 12,
          "font_weight": "bold",
          "text_color": "#2c2c2c"
        },
        {
          "type": "text",
          "text": "نام خانوادگی",
          "font_size": 12,
          "text_color": "#7f7f7f"
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "space-between",
      "margins": {"top": 8}
    },
    {
      "type": "container",
      "items": [
        {
          "type": "text",
          "text": "@{item_daraje}",
          "font_size": 12,
          "font_weight": "bold",
          "text_color": "#2c2c2c"
        },
        {
          "type": "text",
          "text": "درجه",
          "font_size": 12,
          "text_color": "#7f7f7f"
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "space-between",
      "margins": {"top": 8}
    },
    {
      "type": "container",
      "items": [
        {
          "type": "text",
          "text": "@{item_yegan}",
          "font_size": 12,
          "font_weight": "bold",
          "text_color": "#2c2c2c"
        },
        {
          "type": "text",
          "text": "یگان",
          "font_size": 12,
          "text_color": "#7f7f7f"
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "space-between",
      "margins": {"top": 8}
    }
  ],
  "border": {
    "corners_radius": 6,
    "stroke": {"color": "#ececec", "width": 1}
  },
  "paddings": {"top": 12, "bottom": 12, "left": 16, "right": 16},
  "margins": {"top": 8},
  "background": [{"type": "solid", "color": "#ffffff"}]
}
```

### **Submit Button Action**

```json
{
  "type": "container",
  "actions": [{
    "log_id": "submit_punishment",
    "url": "div-action://add_item_from_template?template=punishment_item_template&container_id=punishments_list&item_name=@{name}&item_lastname=@{lastname}&item_daraje=@{daraje}&item_yegan=@{yegan}&item_id=@{personal_code}"
  }],
  "items": [
    {
      "type": "text",
      "text": "ثبت تنبیه",
      "font_size": 14,
      "text_color": "#ffffff"
    }
  ],
  "background": [{"type": "solid", "color": "#193d8d"}],
  "border": {"corners_radius": 6},
  "height": {"type": "fixed", "value": 48},
  "content_alignment_horizontal": "center",
  "content_alignment_vertical": "center"
}
```

### **Container for Items**

```json
{
  "type": "container",
  "id": "punishments_list",
  "items": [],
  "orientation": "vertical",
  "margins": {"top": 16}
}
```

## 🎯 **Action Parameters Explained**

```
div-action://add_item_from_template?
  template=punishment_item_template          ← Template key in database
  &container_id=punishments_list             ← Where to add items
  &item_name=@{name}                         ← Variable: item_name = value of 'name'
  &item_lastname=@{lastname}                 ← Variable: item_lastname = value of 'lastname'
  &item_daraje=@{daraje}                     ← Variable: item_daraje = value of 'daraje'
  &item_yegan=@{yegan}                       ← Variable: item_yegan = value of 'yegan'
  &item_id=@{personal_code}                  ← Variable: item_id = value of 'personal_code'
```

## 📋 **Variable Naming Convention**

**Template variables** should match **action parameters**:

| Template Variable | Action Parameter | Value Source |
|-------------------|------------------|--------------|
| `@{item_name}` | `item_name=@{name}` | Form variable `name` |
| `@{item_family}` | `item_family=@{lastname}` | Form variable `lastname` |
| `@{item_age}` | `item_age=@{daraje}` | Form variable `daraje` |

## 🚀 **Multiple Submissions**

**First Submit:**
- Name: "اكبر", Family: "حيدري", Age: "25"
- Item 1 added to container

**Second Submit:**
- Name: "علی", Family: "احمدی", Age: "30"
- Item 2 added below Item 1

**Third Submit:**
- Name: "محمد", Family: "رضایی", Age: "28"
- Item 3 added below Item 2

Each submission adds a new item to the list!

## 🎨 **Advanced Usage**

### **Insert at Specific Position**

Add new items at the **beginning** of the list:
```
div-action://add_item_from_template?template=punishment_item_template&container_id=punishments_list&position=0&item_name=@{name}&item_family=@{lastname}
```

### **Complex Variables**

Support nested properties:
```json
// Template
{
  "type": "text",
  "text": "@{item_full_info}"
}

// Action
?item_full_info=@{name} @{lastname} - @{age} ساله
```

### **Conditional Content**

Use DivKit expressions in template:
```json
{
  "type": "text",
  "text": "@{item_name}",
  "text_color": "@{item_age > 30 ? '#ff0000' : '#00ff00'}"
}
```

## 🔍 **Debugging**

The system provides comprehensive logging:

```
UIDiv2ActionHandler: ========== ADD ITEM FROM TEMPLATE ==========
UIDiv2ActionHandler: templateKey=punishment_item_template
UIDiv2ActionHandler: containerId=punishments_list
UIDiv2ActionHandler: Variables extracted: {item_name=اكبر, item_family=حيدري, item_age=25}
DynamicPatchGenerator: ========== GENERATING ADD ITEM PATCH ==========
DynamicPatchGenerator: Patch generated successfully
UIDiv2ActionHandler: Patch applied successfully!
```

## ⚠️ **Important Notes**

1. **Template must exist** in database before using the action
2. **Container ID must match** the ID in your page JSON
3. **Variable names** in template must match parameter names in action
4. **All variables** are replaced as strings
5. **Unique IDs** are automatically generated for items

## 🎁 **Benefits of This Approach**

✅ **Fully Offline** - No server needed for patch generation
✅ **Reusable Templates** - One template, many uses
✅ **Dynamic Content** - Add unlimited items
✅ **Type Safe** - Variables are matched by name
✅ **Flexible** - Support any template structure
✅ **Fast** - No network latency
✅ **Clean Code** - No client-side modifications needed

## 📱 **Complete Working Example**

Here's everything you need for your punishment form:

**1. Store template in database** (key: `punishment_item_template`)
**2. Use the page JSON** with form inputs and submit button
**3. Click submit** → Item added automatically
**4. Click submit again** → Another item added
**5. Submit 5 times** → 5 items in the list!

The system handles everything automatically - template loading, variable replacement, patch generation, and application! 🎉
