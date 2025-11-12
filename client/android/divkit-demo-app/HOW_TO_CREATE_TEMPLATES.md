# How to Create Templates - Step by Step

## 🎯 **Your Use Case**

You want templates that work with DivKit's template system, so the generated patch looks like:

```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10",
  "actions_previous": [...]
}
```

## ✅ **Correct Way to Create Templates**

### **Step 1: Add Template to Your Page JSON**

In your main page JSON, add a `templates` section:

```json
{
  "card": {
    "log_id": "my_page",
    "states": [...]
  },
  "templates": {
    "first_box": {
      "type": "container",
      "items": [
        {
          "type": "text",
          "$text": "page_number",
          "font_size": 20
        },
        {
          "type": "text",
          "$text": "all_page_number",
          "font_size": 20
        }
      ]
    }
  }
}
```

### **Step 2: Use the Action**

```
div-action://add_item_from_template?
  template_type=first_box
  &container_id=items_container
  &page_number=9
  &all_page_number=10
```

### **Step 3: System Generates This**

```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10"
}
```

### **Step 4: DivKit Resolves**

DivKit looks up `first_box` in the templates section and replaces `$text: "page_number"` with `text: "9"`.

## 📋 **Template Bindings Reference**

### **Text Binding:**
```json
{
  "type": "text",
  "$text": "property_name"
}
```
Resolved by parameter: `property_name=value`

### **Actions Binding:**
```json
{
  "type": "text",
  "$actions": "actions_property"
}
```
Resolved by parameter: `actions_property=[{...}]`

### **Background Binding:**
```json
{
  "type": "container",
  "$background": "bg_property"
}
```
Resolved by parameter: `bg_property=[{...}]`

### **Any Property Binding:**
```json
{
  "type": "container",
  "$margins": "margins_property"
}
```
Resolved by parameter: `margins_property={...}`

## 🎨 **Your Exact Template**

Based on your example, here's the complete template:

```json
{
  "templates": {
    "first_box": {
      "type": "container",
      "width": {"type": "match_parent"},
      "height": {"type": "fixed", "value": 54},
      "items": [
        {
          "type": "text",
          "text": "قبلی>",
          "$actions": "actions_previous",
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
              "$text": "all_page_number",
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
              "$text": "page_number",
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
  }
}
```

## 🚀 **Usage Example**

**Action to add page 9:**
```
div-action://add_item_from_template?template_type=first_box&container_id=pagination_container&page_number=9&all_page_number=10&actions_previous=[{"log_id":"call_service","url":"div-action://set_patch?patch=patch_8_110801193"}]
```

**Result - Item Added to Patch:**
```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10",
  "actions_previous": [
    {
      "log_id": "call_service",
      "url": "div-action://set_patch?patch=patch_8_110801193"
    }
  ]
}
```

## ✨ **Key Takeaways**

1. **Template is in your page JSON** under `templates` section
2. **Template name** (`first_box`) becomes the `type` in generated items
3. **Properties** in action URL become properties in the generated item
4. **DivKit automatically resolves** `$property` bindings
5. **No database storage needed** for the template (it's in the page)

This is the **proper DivKit way** to use templates! 🎉

