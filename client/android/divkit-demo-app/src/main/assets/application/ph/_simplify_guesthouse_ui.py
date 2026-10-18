# -*- coding: utf-8 -*-
import json
from pathlib import Path

p = Path(__file__).with_name("ph_main_profile_guesthouse_select.json")
data = json.loads(p.read_text(encoding="utf-8"))

data["card"]["states"] = [
    {
        "state_id": 0,
        "div": {
            "type": "container",
            "orientation": "vertical",
            "width": {"type": "match_parent"},
            "height": {"type": "match_parent"},
            "background": [{"type": "solid", "color": "#ffffff"}],
            "border": {"corners_radius": {"top-left": 16, "top-right": 16}},
            "paddings": {"top": 8, "bottom": 16, "start": 16, "end": 16},
            "items": [
                {
                    "type": "separator",
                    "delimiter_style": {"color": "#d1d5db"},
                    "width": {"type": "fixed", "value": 40},
                    "height": {"type": "fixed", "value": 4},
                    "alignment_horizontal": "center",
                    "border": {"corner_radius": 2},
                    "margins": {"bottom": 16},
                },
                {
                    "type": "container",
                    "orientation": "overlap",
                    "width": {"type": "match_parent"},
                    "height": {"type": "wrap_content"},
                    "margins": {"bottom": 16},
                    "items": [
                        {
                            "type": "text",
                            "text": "@{guesthouse_step == 'days' ? 'انتخاب روز' : 'انتخاب مهمانسرا'}",
                            "font_size": 17,
                            "font_weight": "medium",
                            "text_color": "#193d8d",
                            "text_alignment_horizontal": "center",
                            "alignment_horizontal": "center",
                            "width": {"type": "match_parent"},
                            "height": {"type": "wrap_content"},
                        },
                        {
                            "type": "text",
                            "text": "بستن",
                            "font_size": 13,
                            "text_color": "#6b7280",
                            "alignment_horizontal": "left",
                            "actions": [
                                {
                                    "log_id": "close_guesthouse_select",
                                    "url": "div-action://close_bottom_sheet",
                                }
                            ],
                            "width": {"type": "wrap_content"},
                            "height": {"type": "wrap_content"},
                        },
                    ],
                },
                {
                    "type": "container",
                    "orientation": "vertical",
                    "width": {"type": "match_parent"},
                    "height": {"type": "match_parent", "weight": 1},
                    "visibility": "@{guesthouse_step == 'list' ? 'visible' : 'gone'}",
                    "items": [
                        {
                            "type": "input",
                            "text_variable": "guesthouse_search",
                            "hint_text": "جستجو...",
                            "hint_color": "#9ca3af",
                            "font_size": 14,
                            "text_color": "#1f2937",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "match_parent"},
                            "height": {"type": "wrap_content"},
                            "background": [{"type": "solid", "color": "#f3f5f9"}],
                            "border": {"corner_radius": 10},
                            "paddings": {
                                "top": 12,
                                "bottom": 12,
                                "start": 14,
                                "end": 14,
                            },
                            "margins": {"bottom": 12},
                        },
                        {
                            "type": "gallery",
                            "orientation": "vertical",
                            "restrict_parent_scroll": True,
                            "width": {"type": "match_parent"},
                            "height": {"type": "match_parent", "weight": 1},
                            "items": [
                                {
                                    "type": "container",
                                    "orientation": "vertical",
                                    "width": {"type": "match_parent"},
                                    "height": {"type": "wrap_content"},
                                    "item_builder": {
                                        "data": "@{guesthouse_places}",
                                        "data_element_name": "place",
                                        "prototypes": [
                                            {
                                                "selector": "@{len(guesthouse_search) == 0 || contains(getStringFromDict(place, 'description'), guesthouse_search) || contains(getStringFromDict(place, 'id'), guesthouse_search)}",
                                                "div": {
                                                    "type": "guest_card",
                                                    "actions": [
                                                        {
                                                            "log_id": "select_guesthouse",
                                                            "url": "div-action://select_guesthouse?guesthouse_id=@{getStringFromDict(place, 'id')}&name=@{encodeUri(getStringFromDict(place, 'description'))}",
                                                        }
                                                    ],
                                                    "items": [
                                                        {
                                                            "type": "text",
                                                            "text": "@{getStringFromDict(place, 'description')}",
                                                            "font_size": 14,
                                                            "font_weight": "medium",
                                                            "text_color": "#1f2937",
                                                            "text_alignment_horizontal": "right",
                                                            "width": {
                                                                "type": "match_parent"
                                                            },
                                                        }
                                                    ],
                                                },
                                            }
                                        ],
                                    },
                                }
                            ],
                        },
                    ],
                },
                {
                    "type": "container",
                    "orientation": "vertical",
                    "width": {"type": "match_parent"},
                    "height": {"type": "match_parent", "weight": 1},
                    "visibility": "@{guesthouse_step == 'days' ? 'visible' : 'gone'}",
                    "items": [
                        {
                            "type": "text",
                            "text": "@{selected_guesthouse_name}",
                            "font_size": 13,
                            "text_color": "#6b7280",
                            "text_alignment_horizontal": "center",
                            "width": {"type": "match_parent"},
                            "margins": {"bottom": 4},
                        },
                        {
                            "type": "text",
                            "text": "@{len(selected_day1_id) > 0 ? selected_window_label : 'تا ۳ روز پشت‌سرهم انتخاب کنید'}",
                            "font_size": 13,
                            "font_weight": "medium",
                            "text_color": "@{len(selected_day1_id) > 0 ? '#193d8d' : '#9ca3af'}",
                            "text_alignment_horizontal": "center",
                            "width": {"type": "match_parent"},
                            "margins": {"bottom": 12},
                        },
                        {
                            "type": "gallery",
                            "orientation": "vertical",
                            "restrict_parent_scroll": True,
                            "width": {"type": "match_parent"},
                            "height": {"type": "match_parent", "weight": 1},
                            "items": [
                                {
                                    "type": "container",
                                    "orientation": "vertical",
                                    "width": {"type": "match_parent"},
                                    "height": {"type": "wrap_content"},
                                    "item_builder": {
                                        "data": "@{guesthouse_places}",
                                        "data_element_name": "place",
                                        "prototypes": [
                                            {
                                                "selector": "@{getStringFromDict(place, 'id') == selected_guesthouse_id}",
                                                "div": {
                                                    "type": "container",
                                                    "orientation": "vertical",
                                                    "width": {"type": "match_parent"},
                                                    "height": {"type": "wrap_content"},
                                                    "item_builder": {
                                                        "data": "@{getArrayFromDict(place, 'calendarPlanDivisionDetail')}",
                                                        "data_element_name": "day",
                                                        "prototypes": [
                                                            {
                                                                "selector": "@{true}",
                                                                "div": {
                                                                    "type": "day_card",
                                                                    "background": [
                                                                        {
                                                                            "type": "solid",
                                                                            "color": "@{(getStringFromDict(day, 'id') == selected_day1_id || getStringFromDict(day, 'id') == selected_day2_id || getStringFromDict(day, 'id') == selected_day3_id) ? '#193d8d' : '#f3f5f9'}",
                                                                        }
                                                                    ],
                                                                    "actions": [
                                                                        {
                                                                            "log_id": "select_guesthouse_start_day",
                                                                            "url": "div-action://select_guesthouse_start_day?guesthouse_id=@{selected_guesthouse_id}&day_id=@{getStringFromDict(day, 'id')}&date=@{encodeUri(getStringFromDict(day, 'date'))}",
                                                                        }
                                                                    ],
                                                                    "items": [
                                                                        {
                                                                            "type": "text",
                                                                            "text": "@{getStringFromDict(day, 'dayOfWeek')}",
                                                                            "font_size": 14,
                                                                            "font_weight": "medium",
                                                                            "text_color": "@{(getStringFromDict(day, 'id') == selected_day1_id || getStringFromDict(day, 'id') == selected_day2_id || getStringFromDict(day, 'id') == selected_day3_id) ? '#ffffff' : '#1f2937'}",
                                                                            "text_alignment_horizontal": "right",
                                                                            "width": {
                                                                                "type": "match_parent",
                                                                                "weight": 1,
                                                                            },
                                                                        },
                                                                        {
                                                                            "type": "text",
                                                                            "text": "@{getStringFromDict(day, 'date')}",
                                                                            "font_size": 13,
                                                                            "text_color": "@{(getStringFromDict(day, 'id') == selected_day1_id || getStringFromDict(day, 'id') == selected_day2_id || getStringFromDict(day, 'id') == selected_day3_id) ? '#dbe4ff' : '#6b7280'}",
                                                                            "text_alignment_horizontal": "left",
                                                                            "width": {
                                                                                "type": "wrap_content"
                                                                            },
                                                                        },
                                                                    ],
                                                                },
                                                            }
                                                        ],
                                                    },
                                                },
                                            }
                                        ],
                                    },
                                }
                            ],
                        },
                        {
                            "type": "container",
                            "orientation": "horizontal",
                            "width": {"type": "match_parent"},
                            "height": {"type": "wrap_content"},
                            "margins": {"top": 12},
                            "items": [
                                {
                                    "type": "text",
                                    "text": "بازگشت",
                                    "font_size": 14,
                                    "font_weight": "medium",
                                    "text_color": "#193d8d",
                                    "text_alignment_horizontal": "center",
                                    "width": {"type": "match_parent", "weight": 1},
                                    "height": {"type": "wrap_content"},
                                    "background": [
                                        {"type": "solid", "color": "#eef2f8"}
                                    ],
                                    "border": {"corner_radius": 10},
                                    "paddings": {"top": 14, "bottom": 14},
                                    "margins": {"end": 6},
                                    "actions": [
                                        {
                                            "log_id": "back_to_guesthouse_list",
                                            "url": "div-action://select_guesthouse_back",
                                        }
                                    ],
                                },
                                {
                                    "type": "text",
                                    "text": "ادامه",
                                    "font_size": 14,
                                    "font_weight": "medium",
                                    "text_color": "#ffffff",
                                    "text_alignment_horizontal": "center",
                                    "width": {"type": "match_parent", "weight": 1},
                                    "height": {"type": "wrap_content"},
                                    "background": [
                                        {"type": "solid", "color": "#193d8d"}
                                    ],
                                    "border": {"corner_radius": 10},
                                    "paddings": {"top": 14, "bottom": 14},
                                    "margins": {"start": 6},
                                    "visibility": "@{len(selected_day1_id) > 0 ? 'visible' : 'gone'}",
                                    "actions": [
                                        {
                                            "log_id": "confirm_guesthouse_days",
                                            "url": "div-action://call_service?path=guesthouse/receptionDays&sysName=guesthouse&ph/token=empty&guesthouse_id=@{selected_guesthouse_id}&name=@{encodeUri(selected_guesthouse_name)}&day1_id=@{selected_day1_id}&day2_id=@{selected_day2_id}&day3_id=@{selected_day3_id}&day1_date=@{encodeUri(selected_day1_date)}&day2_date=@{encodeUri(selected_day2_date)}&day3_date=@{encodeUri(selected_day3_date)}",
                                        }
                                    ],
                                },
                            ],
                        },
                    ],
                },
            ],
        },
    }
]

data["templates"] = {
    "guest_card": {
        "type": "container",
        "orientation": "vertical",
        "width": {"type": "match_parent"},
        "height": {"type": "wrap_content"},
        "background": [{"type": "solid", "color": "#f8fafc"}],
        "border": {"corner_radius": 12},
        "paddings": {"top": 16, "bottom": 16, "start": 14, "end": 14},
        "margins": {"bottom": 8},
    },
    "day_card": {
        "type": "container",
        "orientation": "horizontal",
        "content_alignment_vertical": "center",
        "width": {"type": "match_parent"},
        "height": {"type": "wrap_content"},
        "border": {"corner_radius": 12},
        "paddings": {"top": 14, "bottom": 14, "start": 16, "end": 16},
        "margins": {"bottom": 8},
    },
}

p.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("ok")
