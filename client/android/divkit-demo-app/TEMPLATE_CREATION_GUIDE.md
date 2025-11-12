# Template Creation Guide

## 📋 **Overview**

Templates support **two types** of variable bindings:
1. **Standard placeholders**: `@{variable_name}` - For text content
2. **DivKit template bindings**: `$text`, `$actions`, etc. - For DivKit properties

## 🎯 **Your Example Explained**

Your template uses DivKit template bindings:

```json
{
  "type": "text",
  "$text": "page_number",        ← DivKit binding
  "font_size": 20
}
```

**What happens:**
- `$text` is a template binding
- `"page_number"` is the variable name to bind
- System replaces `$text: "page_number"` with `text: "actual value"`

## 🔧 **How to Create Templates**

### **Method 1: Using DivKit Template Bindings (Your Style)**

```json
{
  "type": "container",
  "id": "item_@{item_id}",
  "items": [
    {
      "type": "text",
      "$text": "page_number",              ← Will be replaced with "text": value
      "font_size": 20
    },
    {
      "type": "text",
      "$text": "all_page_number",          ← Will be replaced with "text": value
      "font_size": 20
    },
    {
      "type": "text",
      "text": "قبلی>",
      "$actions": "actions_previous",      ← Will be replaced with "actions": value
      "font_size": 20
    }
  ]
}
```

**Action to use this template:**
```
div-action://add_item_from_template?
  template=pagination_template
  &container_id=items_container
  &page_number=5
  &all_page_number=10
  &actions_previous=[{"url":"div-action://next_page"}]
  &item_id=page_5
```

### **Method 2: Using Standard Placeholders (Simpler)**

```json
{
  "type": "container",
  "id": "item_@{item_id}",
  "items": [
    {
      "type": "text",
      "text": "@{page_number}",
      "font_size": 20
    },
    {
      "type": "text",
      "text": "از",
      "font_size": 20
    },
    {
      "type": "text",
      "text": "@{all_page_number}",
      "font_size": 20
    }
  ]
}
```

**Action:**
```
div-action://add_item_from_template?
  template=pagination_template
  &container_id=items_container
  &page_number=5
  &all_page_number=10
  &item_id=page_5
```

### **Method 3: Mixed (Both Types)**

```json
{
  "type": "container",
  "items": [
    {
      "type": "text",
      "$text": "dynamic_title",           ← DivKit binding
      "text_color": "@{item_color}"        ← Placeholder in property value
    },
    {
      "type": "text",
      "text": "نام: @{item_name}",         ← Placeholder in text content
      "font_size": 14
    }
  ]
}
```

## 📝 **For Your Pagination Template**

Here's how to create a proper template based on your example:

### **Template JSON (Store with key: `pagination_template`)**

```json
{
  "type": "container",
  "id": "pagination_@{item_id}",
  "width": {"type": "match_parent"},
  "height": {"type": "fixed", "value": 54},
  "items": [
    {
      "type": "text",
      "text": "قبلی>",
      "actions": @{actions_previous},
      "font_size": 20,
      "width": {"type": "fixed", "value": 79},
      "text_alignment_horizontal": "start",
      "margins": {"start": 10, "end": 10},
      "alignment_horizontal": "start",
      "alignment_vertical": "center"
    },
    {
      "type": "container",
      "width": {"type": "wrap_content"},
      "height": {"type": "match_parent"},
      "items": [
        {
          "type": "text",
          "text": "@{all_page_number}",
          "font_size": 20,
          "width": {"type": "wrap_content"},
          "text_alignment_horizontal": "center",
          "alignment_vertical": "center"
        },
        {
          "type": "text",
          "text": "از",
          "font_size": 20,
          "width": {"type": "fixed", "value": 44},
          "alignment_horizontal": "center",
          "alignment_vertical": "center",
          "text_alignment_horizontal": "center"
        },
        {
          "type": "text",
          "text": "@{page_number}",
          "font_size": 20,
          "width": {"type": "wrap_content"},
          "alignment_vertical": "center",
          "text_alignment_horizontal": "center",
          "margins": {"end": 20}
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "center",
      "content_alignment_vertical": "center",
      "alignment_vertical": "center",
      "alignment_horizontal": "end"
    }
  ],
  "orientation": "overlap",
  "content_alignment_horizontal": "center",
  "content_alignment_vertical": "center",
  "background": [{"type": "solid", "color": "#e9e9e9"}],
  "margins": {"right": 20, "bottom": 20, "left": 20},
  "border": {
    "corner_radius": 12,
    "corners_radius": {
      "top-right": 21,
      "bottom-left": 20,
      "bottom-right": 19,
      "top-left": 21
    }
  }
}
```

## ⚠️ **Important Note About DivKit Template Bindings**

Your original template uses `$text` and `$actions`, which are **DivKit's template system features**. These work differently:

**DivKit Template (`$` prefix):**
- Used with the `"templates"` section in DivKit
- Bindings like `$text`, `$actions` are replaced when the template is instantiated
- Requires the parent JSON to have a `"templates"` section

**Our System:**
- Replaces `@{variable}` placeholders with actual values
- Works with any JSON structure
- Doesn't require DivKit's template system

### **Converting Your Template**

**Option 1: Use Standard Placeholders (Recommended)**

Replace DivKit bindings with standard placeholders:

```json
{
  "type": "text",
  "text": "@{page_number}",      ← Instead of "$text": "page_number"
  "font_size": 20
}
```

**Option 2: Keep DivKit Bindings (If You Need Them)**

If you want to use DivKit's template system, wrap your items in the templates section:

```json
{
  "templates": {
    "pagination_item": {
      "type": "container",
      "items": [
        {
          "type": "text",
          "$text": "page_number",
          "font_size": 20
        }
      ]
    }
  }
}
```

## ✅ **Recommended Template Format for Your Use Case**

Based on your pagination example, here's the proper format:

```json
{
  "type": "container",
  "id": "pagination_@{item_id}",
  "width": {"type": "match_parent"},
  "height": {"type": "fixed", "value": 54},
  "items": [
    {
      "type": "text",
      "text": "قبلی>",
      "font_size": 20,
      "width": {"type": "fixed", "value": 79},
      "text_alignment_horizontal": "start",
      "margins": {"start": 10, "end": 10},
      "alignment_horizontal": "start",
      "alignment_vertical": "center"
    },
    {
      "type": "container",
      "width": {"type": "wrap_content"},
      "height": {"type": "match_parent"},
      "items": [
        {
          "type": "text",
          "text": "@{all_page_number}",
          "width": {"type": "wrap_content"},
          "height": {"type": "wrap_content"},
          "text_alignment_horizontal": "center",
          "alignment_vertical": "center",
          "font_size": 20
        },
        {
          "type": "text",
          "text": "از",
          "font_size": 20,
          "width": {"type": "fixed", "value": 44},
          "alignment_horizontal": "center",
          "alignment_vertical": "center",
          "text_alignment_horizontal": "center"
        },
        {
          "type": "text",
          "text": "@{page_number}",
          "font_size": 20,
          "width": {"type": "wrap_content"},
          "alignment_vertical": "center",
          "text_alignment_horizontal": "center",
          "margins": {"end": 20}
        }
      ],
      "orientation": "horizontal",
      "content_alignment_horizontal": "center",
      "content_alignment_vertical": "center",
      "alignment_vertical": "center",
      "alignment_horizontal": "end"
    }
  ],
  "content_alignment_horizontal": "center",
  "content_alignment_vertical": "center",
  "orientation": "overlap",
  "background": [{"type": "solid", "color": "#e9e9e9"}],
  "margins": {"right": 20, "bottom": 20, "left": 20},
  "border": {
    "corner_radius": 12,
    "corners_radius": {
      "top-right": 21,
      "bottom-left": 20,
      "bottom-right": 19,
      "top-left": 21
    }
  }
}
```

**Action:**
```
div-action://add_item_from_template?
  template=pagination_template
  &container_id=pagination_container
  &page_number=5
  &all_page_number=10
  &item_id=page_5
```

## 🎯 **Simple Conversion Rules**

| Original (DivKit Template) | Converted (Our System) |
|---------------------------|------------------------|
| `"$text": "page_number"` | `"text": "@{page_number}"` |
| `"$actions": "my_actions"` | `"actions": @{my_actions}` |
| `"$background": "bg_color"` | `"background": @{bg_color}` |

## 📊 **Comparison**

### **DivKit Template System** (`$text`)
- Requires `templates` section in JSON
- Used for reusable components within one JSON
- More complex setup

### **Our Placeholder System** (`@{variable}`)
- Simple variable replacement
- Works with any JSON structure
- No special setup needed
- ✅ **Recommended for your use case**

## 💡 **Best Practice**

For dynamic item addition, use **standard placeholders** (`@{variable_name}`):

✅ **Do this:**
```json
{
  "type": "text",
  "text": "@{item_name}"
}
```

❌ **Avoid this (for dynamic items):**
```json
{
  "type": "text",
  "$text": "item_name"
}
```

The standard placeholder format is simpler, clearer, and works perfectly with the dynamic patch generation system!

## 🚀 **Ready-to-Use Template**

Here's a clean template based on your example (without `$` bindings):

```json
{
  "type": "container",
  "id": "page_item_@{item_id}",
  "width": {"type": "match_parent"},
  "height": {"type": "fixed", "value": 54},
  "items": [
    {
      "type": "text",
      "text": "صفحه @{page_number} از @{all_page_number}",
      "font_size": 16,
      "text_alignment_horizontal": "center",
      "alignment_vertical": "center"
    }
  ],
  "background": [{"type": "solid", "color": "#e9e9e9"}],
  "margins": {"top": 8},
  "border": {"corners_radius": 12}
}
```

Simple, clean, and works perfectly! 🎉

