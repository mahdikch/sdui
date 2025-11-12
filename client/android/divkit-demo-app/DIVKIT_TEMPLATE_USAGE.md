# DivKit Template Usage with Dynamic Patch Generation

## 🎯 **Overview**

This system now supports **DivKit's native template system**, where templates are defined in the `templates` section and items reference them by `type`.

## 📋 **How DivKit Templates Work**

### **Step 1: Define Template in Your Page**

In your main page JSON, add a `templates` section:

```json
{
  "card": {
    "log_id": "my_page",
    "states": [{
      "state_id": 0,
      "div": {
        "type": "container",
        "items": [
          {
            "type": "container",
            "id": "items_container",
            "items": [],
            "orientation": "vertical"
          }
        ]
      }
    }]
  },
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
          "font_size": 20
        },
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "$text": "all_page_number",
              "font_size": 20
            },
            {
              "type": "text",
              "text": "از",
              "font_size": 20
            },
            {
              "type": "text",
              "$text": "page_number",
              "font_size": 20
            }
          ],
          "orientation": "horizontal"
        }
      ],
      "orientation": "overlap",
      "background": [{"type": "solid", "color": "#e9e9e9"}]
    }
  }
}
```

### **Step 2: Use the Action**

When you want to add an item using the template:

```
div-action://add_item_from_template?
  template_type=first_box
  &container_id=items_container
  &page_number=9
  &all_page_number=10
  &actions_previous=[{"log_id":"call_service","url":"div-action://set_patch?patch=patch_8_110801193"}]
```

### **Step 3: System Generates Patch**

The system automatically generates this patch item:

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

## 🎨 **Template Creation**

### **Your Pagination Template**

**In your page's `templates` section:**

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

## 🔧 **Action Format**

```
div-action://add_item_from_template?
  template_type=[templateName]     ← Name from templates section
  &container_id=[containerId]      ← Where to add the item
  &[property1]=[value1]            ← Properties for the template
  &[property2]=[value2]            ← More properties
  &position=[number]               ← Optional: insert position
```

## 📝 **Complete Example**

### **Your Page JSON:**

```json
{
  "card": {
    "log_id": "pagination_page",
    "states": [{
      "state_id": 0,
      "div": {
        "type": "container",
        "items": [
          {
            "type": "text",
            "text": "صفحات",
            "font_size": 18,
            "font_weight": "bold"
          },
          {
            "type": "container",
            "id": "pagination_container",
            "items": [],
            "orientation": "vertical",
            "margins": {"top": 16}
          }
        ],
        "orientation": "vertical",
        "paddings": {"top": 24, "bottom": 24, "left": 24, "right": 24}
      }
    }],
    "variables": []
  },
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
          "font_size": 20
        },
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "$text": "all_page_number",
              "font_size": 20
            },
            {
              "type": "text",
              "text": "از",
              "font_size": 20
            },
            {
              "type": "text",
              "$text": "page_number",
              "font_size": 20
            }
          ],
          "orientation": "horizontal"
        }
      ],
      "orientation": "overlap",
      "background": [{"type": "solid", "color": "#e9e9e9"}],
      "margins": {"top": 8}
    }
  }
}
```

### **Add Items Using Actions:**

**Page 1:**
```
div-action://add_item_from_template?
  template_type=first_box
  &container_id=pagination_container
  &page_number=1
  &all_page_number=10
  &actions_previous=[{"log_id":"prev","url":"div-action://show_toast?message=Previous"}]
```

**Page 2:**
```
div-action://add_item_from_template?
  template_type=first_box
  &container_id=pagination_container
  &page_number=2
  &all_page_number=10
  &actions_previous=[{"log_id":"prev","url":"div-action://set_patch?patch=patch_1"}]
```

## 🎯 **Key Points**

### **Template Bindings (`$property`):**
- `$text` - Binds to the `text` property
- `$actions` - Binds to the `actions` property
- `$background` - Binds to the `background` property
- Any property can be bound with `$propertyName`

### **How It Works:**

**Template Definition:**
```json
{
  "type": "text",
  "$text": "page_number"
}
```

**Action Parameter:**
```
&page_number=9
```

**Generated Item:**
```json
{
  "type": "first_box",
  "page_number": "9"
}
```

**DivKit Resolves To:**
```json
{
  "type": "text",
  "text": "9"    ← $text binding resolved
}
```

## 🔄 **Complete Flow**

1. **Define template** in page's `templates` section with name `first_box`
2. **Use bindings** like `$text`, `$actions` in template
3. **Trigger action** with `template_type=first_box`
4. **System creates item** with `type: "first_box"` and all properties
5. **DivKit resolves** the template bindings automatically
6. **Item appears** in the container!

## 📊 **Two Approaches Supported**

### **Approach 1: Template Type Reference (Your Way)**

✅ **Best for:** Using existing DivKit templates in your page
✅ **Advantages:** Clean separation, reusable templates, DivKit native

**Action:**
```
template_type=first_box&page_number=9&all_page_number=10
```

**Result:**
```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10"
}
```

### **Approach 2: Full Template from Database**

✅ **Best for:** Templates not in the page's templates section
✅ **Advantages:** Flexible, can use different templates per item

**Action:**
```
template=my_template_key&item_name=اكبر&item_family=حيدري
```

**Result:**
Template loaded from DB, variables replaced, complete item added.

## 🎨 **Example: Punishment List Template**

### **In Your Page's `templates` Section:**

```json
{
  "templates": {
    "punishment_item": {
      "type": "container",
      "items": [
        {
          "type": "container",
          "items": [
            {
              "type": "text",
              "$text": "item_name",
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
              "$text": "item_family",
              "font_size": 12,
              "font_weight": "bold"
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
        }
      ],
      "border": {"stroke": {"color": "#ececec", "width": 1}},
      "paddings": {"top": 12, "bottom": 12, "left": 16, "right": 16},
      "margins": {"top": 8}
    }
  }
}
```

### **Submit Button Action:**

```json
{
  "actions": [{
    "url": "div-action://add_item_from_template?template_type=punishment_item&container_id=punishments_list&item_name=@{name}&item_family=@{lastname}"
  }]
}
```

## ✨ **Benefits**

1. ✅ **No database storage needed** - Templates in page JSON
2. ✅ **DivKit native** - Uses official template system
3. ✅ **Type safe** - Template bindings are checked by DivKit
4. ✅ **Efficient** - Templates compiled once, reused many times
5. ✅ **Clean separation** - Templates separate from logic

## 🚀 **Quick Reference**

**Action Format:**
```
div-action://add_item_from_template?template_type=YOUR_TEMPLATE_NAME&container_id=YOUR_CONTAINER&property1=value1&property2=value2
```

**Generated Item:**
```json
{
  "type": "YOUR_TEMPLATE_NAME",
  "property1": "value1",
  "property2": "value2"
}
```

**DivKit automatically resolves** the template bindings using the properties you provide!

Now you can use DivKit templates exactly as intended! 🎉

