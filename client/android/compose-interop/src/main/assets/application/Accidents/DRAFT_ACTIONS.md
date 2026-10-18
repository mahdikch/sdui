# راهنمای Draft آفلاین — فرآیند تصادفات سازشی

## کلیدهای DB

| کلید | توضیح |
|------|--------|
| `current_accident_id` | UUID یکتا برای هر فرآیند |
| `draft/{accident_id}/page1` | draft صفحه ۱ (compromisefirst) |
| `draft/{accident_id}/page2` | draft صفحه ۲ (vehicleInformationEntry) |
| `draft/{accident_id}/page3` | draft صفحه ۳ (exportreasoninginference) |
| `draft/{accident_id}/page4` | draft صفحه ۴ (finalregistration) |
| `{username}/Accidents/{date}` | رکورد لیست بایگانی |

## مسیر صفحات (load_screen)

| فایل asset | کلید DB |
|------------|---------|
| `Accidents/main.json` | `Accidents/main` |
| `Accidents/compromisefirst.json` | `Accidents/compromisefirst` |
| `Accidents/vehicleInformationEntry.json` | `Accidents/vehicleInformationEntry` |
| `Accidents/exportreasoninginference.json` | `Accidents/exportreasoninginference` |
| `Accidents/finalregistration.json` | `Accidents/finalregistration` |
| `Accidents/archive.json` | `Accidents/archive` |

## اکشن‌های استفاده‌شده

### ۱. شروع فرآیند (main → page1)

```
div-action://set_random_id_to_db?key=current_accident_id
div-action://set_variable_to_db?key=process_type&value=sazeshi
div-action://set_variable_to_db?key=current_step&value=1
div-action://load_screen?screen=Accidents/compromisefirst
```

### ۲. ذخیره draft صفحه (هر «بعدی»)

```
div-action://set_page_with_changes_to_db?key=draft/@{current_accident_id}/pageN
```

### ۳. ثبت در بایگانی (کنار هر save)

```
div-action://set_object_to_db?archive_path=Accidents&next=draft/@{current_accident_id}/pageN&title=ثبت تصادف سازشی&accident_id=@{current_accident_id}&current_step=N&process_type=sazeshi&step_dbshow=...&step_persian_lan=مرحله&accident_id_dbshow=@{current_accident_id}&accident_id_persian_lan=شناسه
```

> **archive_path=Accidents** — برای نمایش در `offline_list_container`  
> **next=** — کلید draft برای resume (کلیک «ادامه» در بایگانی)

### ۴. بارگذاری accident_id در هر صفحه (visibility_actions)

```
div-action://set_variable_from_db?name=current_accident_id
```

### ۵. باز کردن بایگانی

```
div-action://load_screen?screen=Accidents/archive
```

### ۶. خروج / ذخیره ناقص (دکمه انصراف)

```
div-action://set_page_with_changes_to_db?key=draft/@{current_accident_id}/pageN
div-action://set_object_to_db?archive_path=Accidents&next=draft/...&title=...
div-action://load_screen?screen=Accidents/main
```

### ۷. Resume از بایگانی (خودکار)

کلیک روی آیتم → `loadeScreenWithData(data, next)` → بارگذاری JSON از کلید `next` (draft پر شده)

### ۸. پاک‌سازی بعد از submit موفق (page4)

```
div-action://delet_variables_frome_db?draft/@{current_accident_id}/page1&draft/@{current_accident_id}/page2&draft/@{current_accident_id}/page3&draft/@{current_accident_id}/page4
div-action://load_screen?screen=Accidents/main
```

## نصب در DB

صفحات asset را با کلید DB متناظر insert کنید، مثلاً:

- `Accidents/main` ← محتوای `Accidents/main.json`
- `Accidents/compromisefirst` ← `Accidents/compromisefirst.json`
- ...

## فلو

```
main [شروع سازشی]
  → set_random_id_to_db
  → page1 [compromisefirst]
  → [بعدی] save draft page1 + archive + page2
  → [بعدی] save draft page2 + archive + page3
  → [بعدی] save draft page3 + archive + page4
  → [submit] save draft page4

main [بایگانی] → archive → offline_list → resume draft
```
