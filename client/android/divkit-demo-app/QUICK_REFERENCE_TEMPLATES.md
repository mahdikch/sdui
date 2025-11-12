# Quick Reference: Dynamic Item Addition with DivKit Templates

## 🚀 **Two Ways to Use Templates**

### **Method 1: DivKit Template Reference (Recommended for Your Case)**

✅ Template defined in page's `templates` section
✅ Items reference template by `type`
✅ Clean and efficient

**Setup:**
1. Add template to your page JSON under `templates`
2. Use `template_type` in action

**Action:**
```
div-action://add_item_from_template?
  template_type=first_box
  &container_id=items_container
  &page_number=9
  &all_page_number=10
  &actions_previous=[{"log_id":"prev","url":"div-action://..."}]
```

**Result:**
```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10",
  "actions_previous": [...]
}
```

---

### **Method 2: Full Template from Database**

✅ Template stored in database
✅ Supports variable replacement with `@{variable}`
✅ Flexible for dynamic templates

**Setup:**
1. Store template JSON in database
2. Use `template` parameter in action

**Action:**
```
div-action://add_item_from_template?
  template=my_template_key
  &container_id=items_container
  &item_name=اكبر
  &item_family=حيدري
```

**Result:**
Full template loaded, variables replaced, and added as complete item

---

## 📋 **Action Parameters**

### **Required:**
- `container_id` - ID of container to add items to

### **Choose One:**
- `template_type` - Name of template from page's `templates` section
- `template` - Key of template stored in database

### **Optional:**
- `position` - Insert position (0=beginning, null=end)
- Any other parameters become properties/variables

---

## 🎯 **Your Pagination Example**

**Page JSON:**
```json
{
  "card": {...},
  "templates": {
    "first_box": {
      "type": "container",
      "items": [
        {"type": "text", "$text": "page_number"},
        {"type": "text", "$text": "all_page_number"},
        {"type": "text", "text": "قبلی>", "$actions": "actions_previous"}
      ],
      "background": [{"type": "solid", "color": "#e9e9e9"}]
    }
  }
}
```

**Action:**
```
div-action://add_item_from_template?template_type=first_box&container_id=pagination_container&page_number=9&all_page_number=10&actions_previous=[{"url":"..."}]
```

**Generated:**
```json
{
  "type": "first_box",
  "page_number": "9",
  "all_page_number": "10",
  "actions_previous": [{"url":"..."}]
}
```

---

## 💡 **Pro Tips**

1. Use `template_type` when template is in page JSON
2. Use `template` when template is in database
3. Complex values (arrays, objects) are automatically parsed
4. Static text (like "از") stays in template, no parameter needed
5. `$property` bindings are resolved by DivKit automatically

---

## ✨ **Benefits**

- ✅ Fully offline
- ✅ No server required
- ✅ DivKit native
- ✅ Type safe
- ✅ Reusable
- ✅ Efficient

**Ready to use!** 🎉

