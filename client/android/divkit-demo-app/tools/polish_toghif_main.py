import json
from copy import deepcopy

p = r"D:\Dev\sdui\sdui\client\android\divkit-demo-app\src\main\assets\application\toghif\toghif_main.json"
with open(p, encoding="utf-8") as f:
    data = json.load(f)


def find_by_visibility(node, needle, path="root"):
    found = []
    if isinstance(node, dict):
        vis = node.get("visibility")
        if isinstance(vis, str) and needle in vis:
            found.append((path, node))
        for k, v in node.items():
            found.extend(find_by_visibility(v, needle, f"{path}.{k}"))
    elif isinstance(node, list):
        for i, v in enumerate(node):
            found.extend(find_by_visibility(v, needle, f"{path}[{i}]"))
    return found


for needle in [
    "variable_car_info_visibility",
    "car_offline_info",
    "variable_driver_info_visibility",
    "parking_visibility",
]:
    hits = find_by_visibility(data, needle)
    print(needle, "hits", len(hits))
    for path, node in hits[:4]:
        print(" ", path, "h=", node.get("height"), "orient=", node.get("orientation"), "nitems=", len(node.get("items") or []))


def make_field_row(label_expr, value_expr, value_color="#252424", alt=False, value_extra=None):
    value_node = {
        "type": "text",
        "text": value_expr,
        "font_size": 13,
        "font_weight": "medium",
        "line_height": 20,
        "letter_spacing": 0,
        "text_color": value_color,
        "text_alignment_horizontal": "left",
        "text_alignment_vertical": "center",
        "width": {"type": "match_parent", "weight": 1},
        "height": {"type": "wrap_content"},
        "margins": {"end": 8},
    }
    if value_extra:
        value_node.update(value_extra)
    return {
        "type": "container",
        "orientation": "horizontal",
        "width": {"type": "match_parent"},
        "height": {"type": "wrap_content"},
        "content_alignment_vertical": "center",
        "content_alignment_horizontal": "center",
        "paddings": {"top": 12, "bottom": 12, "left": 14, "right": 14},
        "margins": {"left": 12, "right": 12, "top": 0, "bottom": 0},
        "background": [{"type": "solid", "color": "#f3f5f9" if alt else "#ffffff"}],
        "border": {"corner_radius": 8},
        "items": [
            value_node,
            {
                "type": "text",
                "text": label_expr,
                "font_size": 12,
                "line_height": 18,
                "letter_spacing": 0,
                "text_color": "#6b7280",
                "text_alignment_horizontal": "right",
                "text_alignment_vertical": "center",
                "width": {"type": "wrap_content"},
                "height": {"type": "wrap_content"},
            },
        ],
    }


def polish_car_info(node):
    """Replace overlap/fixed car info card with vertical wrap_content rows."""
    detail_actions = None
    # try preserve detail link actions from old structure
    try:
        for child in node.get("items") or []:
            if child.get("actions"):
                detail_actions = child.get("actions")
                break
            for nested in child.get("items") or []:
                if nested.get("actions") and "vt/plate" in str(nested.get("actions")):
                    detail_actions = nested.get("actions")
                    break
    except Exception:
        pass
    if detail_actions is None:
        detail_actions = [
            {
                "log_id": "load_screen",
                "url": "div-action://bottom_sheet_div?jsonName=vt/plate",
            }
        ]

    order_value = {
        "type": "text",
        "text": "@{variable_order_information}",
        "font_size": 13,
        "font_weight": "medium",
        "line_height": 20,
        "letter_spacing": 0,
        "text_color": "@{order_information_status?'#d73333':'#252424'}",
        "text_alignment_horizontal": "left",
        "text_alignment_vertical": "center",
        "width": {"type": "match_parent", "weight": 1},
        "height": {"type": "wrap_content"},
        "margins": {"end": 8},
        "paddings": {"top": 4, "bottom": 4, "left": 8, "right": 8},
        "background": [
            {
                "type": "solid",
                "color": "@{order_information_status?'#fbebeb':'#f3f5f9'}",
            }
        ],
        "border": {"corner_radius": 4},
    }

    ticket_value = {
        "type": "container",
        "orientation": "horizontal",
        "width": {"type": "match_parent", "weight": 1},
        "height": {"type": "wrap_content"},
        "content_alignment_vertical": "center",
        "items": [
            {
                "type": "text",
                "text": "@{text___19}",
                "font_size": 12,
                "line_height": 18,
                "text_color": "#252424",
                "width": {"type": "wrap_content"},
                "height": {"type": "wrap_content"},
            },
            {
                "type": "text",
                "text": "@{variable_car_total_ticket_information_vt}",
                "font_size": 14,
                "font_weight": "medium",
                "line_height": 20,
                "text_color": "#252424",
                "width": {"type": "wrap_content"},
                "height": {"type": "wrap_content"},
                "margins": {"start": 4},
            },
        ],
    }

    node.clear()
    node.update(
        {
            "visibility": "@{variable_car_info_visibility}",
            "type": "container",
            "orientation": "vertical",
            "width": {"type": "match_parent"},
            "height": {"type": "wrap_content"},
            "background": [{"type": "solid", "color": "#ffffff"}],
            "border": {
                "corner_radius": 12,
                "stroke": {"color": "#e8ecf4", "width": 1},
            },
            "margins": {"top": 12, "bottom": 8, "left": 12, "right": 12},
            "paddings": {"top": 4, "bottom": 10},
            "items": [
                {
                    "type": "container",
                    "orientation": "horizontal",
                    "width": {"type": "match_parent"},
                    "height": {"type": "wrap_content"},
                    "content_alignment_vertical": "center",
                    "paddings": {"top": 10, "bottom": 10, "left": 14, "right": 14},
                    "items": [
                        {
                            "type": "container",
                            "orientation": "horizontal",
                            "width": {"type": "wrap_content"},
                            "height": {"type": "wrap_content"},
                            "content_alignment_vertical": "center",
                            "actions": detail_actions,
                            "items": [
                                {
                                    "type": "image",
                                    "image_url": "@{arrow_left_blue}",
                                    "height": {"type": "fixed", "value": 12},
                                    "width": {"type": "fixed", "value": 12},
                                },
                                {
                                    "type": "text",
                                    "text": "@{text___13}",
                                    "font_size": 12,
                                    "font_weight": "medium",
                                    "line_height": 18,
                                    "text_color": "#193d8d",
                                    "width": {"type": "wrap_content"},
                                    "height": {"type": "wrap_content"},
                                    "margins": {"start": 6},
                                },
                            ],
                        },
                        {
                            "type": "text",
                            "text": "@{text___12}",
                            "font_size": 14,
                            "font_weight": "medium",
                            "line_height": 22,
                            "text_color": "#193d8d",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "match_parent", "weight": 1},
                            "height": {"type": "wrap_content"},
                        },
                    ],
                },
                make_field_row("@{text___14}", "@{variable_car_model_information_vt}", alt=True),
                make_field_row("@{text___16}", "@{variable_car_color_information_vt}", alt=False),
                {
                    "type": "container",
                    "orientation": "horizontal",
                    "width": {"type": "match_parent"},
                    "height": {"type": "wrap_content"},
                    "content_alignment_vertical": "center",
                    "paddings": {"top": 12, "bottom": 12, "left": 14, "right": 14},
                    "margins": {"left": 12, "right": 12},
                    "background": [{"type": "solid", "color": "#f3f5f9"}],
                    "border": {"corner_radius": 8},
                    "items": [
                        ticket_value,
                        {
                            "type": "text",
                            "text": "@{text___18}",
                            "font_size": 12,
                            "line_height": 18,
                            "text_color": "#6b7280",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "wrap_content"},
                            "height": {"type": "wrap_content"},
                        },
                    ],
                },
                {
                    "type": "container",
                    "orientation": "horizontal",
                    "width": {"type": "match_parent"},
                    "height": {"type": "wrap_content"},
                    "content_alignment_vertical": "center",
                    "paddings": {"top": 12, "bottom": 12, "left": 14, "right": 14},
                    "margins": {"left": 12, "right": 12, "top": 0, "bottom": 4},
                    "background": [{"type": "solid", "color": "#ffffff"}],
                    "border": {"corner_radius": 8},
                    "items": [
                        order_value,
                        {
                            "type": "text",
                            "text": "@{text___21}",
                            "font_size": 12,
                            "line_height": 18,
                            "text_color": "#6b7280",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "wrap_content"},
                            "height": {"type": "wrap_content"},
                        },
                    ],
                },
            ],
        }
    )


def polish_header(header_node):
    """Align header with polished VT/PH style while keeping toghif actions."""
    # Collect refresh actions and toggle actions from existing tree
    refresh_actions = None
    toggle_block = None
    title_text = "@{text___57}"

    def dig(n):
        nonlocal refresh_actions, toggle_block, title_text
        if not isinstance(n, dict):
            return
        if n.get("image_url") == "@{refresh.png}" and n.get("actions"):
            refresh_actions = n["actions"]
        if n.get("content_alignment_horizontal") == "@{loc}" and n.get("actions"):
            toggle_block = n
        if n.get("type") == "text" and n.get("text") == "@{text___57}":
            title_text = n["text"]
        for v in n.get("items") or []:
            dig(v)
        # nested weird structure
        for v in n.values():
            if isinstance(v, list):
                for x in v:
                    dig(x)

    dig(header_node)
    if refresh_actions is None:
        refresh_actions = []
    if toggle_block is None:
        toggle_block = {
            "type": "container",
            "width": {"type": "fixed", "value": 40},
            "height": {"type": "fixed", "value": 22},
            "content_alignment_horizontal": "@{loc}",
            "content_alignment_vertical": "center",
            "items": [
                {
                    "type": "text",
                    "text": " ",
                    "width": {"type": "fixed", "value": 16},
                    "height": {"type": "fixed", "value": 16},
                    "border": {"corner_radius": 8},
                    "margins": {"left": 4, "right": 4},
                    "background": [{"type": "solid", "color": "#ffffff"}],
                }
            ],
            "border": {"corner_radius": 16},
            "background": [{"type": "solid", "color": "@{on_color}"}],
            "actions": [],
        }

    # normalize toggle
    toggle = deepcopy(toggle_block)
    toggle["width"] = {"type": "fixed", "value": 40}
    toggle["height"] = {"type": "fixed", "value": 22}

    header_node.clear()
    header_node.update(
        {
            "type": "container",
            "items": [
                {
                    "type": "image",
                    "image_url": "@{arrow_right.png}",
                    "actions": [
                        {
                            "log_id": "load_screen",
                            "url": "div-action://pop_and_load_screen",
                        }
                    ],
                    "width": {"type": "fixed", "value": 24},
                    "height": {"type": "fixed", "value": 24},
                    "preload_required": True,
                    "alignment_horizontal": "right",
                    "alignment_vertical": "center",
                    "margins": {"right": 12},
                },
                {
                    "type": "container",
                    "orientation": "horizontal",
                    "alignment_horizontal": "left",
                    "alignment_vertical": "center",
                    "width": {"type": "wrap_content"},
                    "height": {"type": "wrap_content"},
                    "margins": {"left": 12},
                    "content_alignment_vertical": "center",
                    "items": [
                        {
                            "type": "image",
                            "image_url": "@{rotate_right.png}",
                            "actions": refresh_actions,
                            "width": {"type": "fixed", "value": 24},
                            "height": {"type": "fixed", "value": 24},
                            "preload_required": True,
                            "margins": {"end": 12},
                        },
                        toggle,
                    ],
                },
                {
                    "type": "text",
                    "text": title_text,
                    "font_size": 16,
                    "font_weight": "medium",
                    "line_height": 24,
                    "letter_spacing": 0,
                    "text_color": "#193d8d",
                    "text_alignment_horizontal": "center",
                    "text_alignment_vertical": "center",
                    "width": {"type": "wrap_content"},
                    "height": {"type": "wrap_content"},
                    "alignment_horizontal": "center",
                    "alignment_vertical": "center",
                },
            ],
            "background": [{"type": "solid", "color": "#ffffff"}],
            "border": {"stroke": {"color": "#e8ecf4", "width": 1}},
            "height": {"type": "fixed", "value": 56},
            "width": {"type": "match_parent"},
            "orientation": "overlap",
            "content_alignment_vertical": "center",
            "content_alignment_horizontal": "center",
            "alignment_horizontal": "center",
            "alignment_vertical": "top",
        }
    )


def polish_plate_type(node):
    """Improve plate-type selector card."""
    actions = node.get("actions")
    # find patch text
    patch_text = "@{patch}"
    for child in node.get("items") or []:
        if child.get("type") == "container":
            for t in child.get("items") or []:
                if t.get("type") == "text" and t.get("text") == "@{patch}":
                    patch_text = "@{patch}"
    node.clear()
    node.update(
        {
            "type": "container",
            "orientation": "horizontal",
            "width": {"type": "match_parent"},
            "height": {"type": "wrap_content"},
            "content_alignment_vertical": "center",
            "content_alignment_horizontal": "left",
            "background": [{"type": "solid", "color": "#ffffff"}],
            "actions": actions
            or [
                {
                    "log_id": "show_bottom_sheet_list",
                    "url": "div-action://bottom_sheet_div?jsonName=vt/plates/bottomsheet",
                }
            ],
            "border": {
                "corner_radius": 12,
                "stroke": {"color": "#e8ecf4", "width": 1},
            },
            "paddings": {"top": 12, "bottom": 12, "left": 14, "right": 14},
            "margins": {"top": 12, "right": 12, "left": 12},
            "items": [
                {
                    "type": "image",
                    "image_url": "@{arrow_down.png}",
                    "height": {"type": "fixed", "value": 16},
                    "width": {"type": "fixed", "value": 16},
                    "alignment_horizontal": "left",
                    "alignment_vertical": "center",
                },
                {
                    "type": "container",
                    "orientation": "vertical",
                    "width": {"type": "match_parent", "weight": 1},
                    "height": {"type": "wrap_content"},
                    "content_alignment_horizontal": "right",
                    "margins": {"start": 10},
                    "items": [
                        {
                            "type": "text",
                            "text": "نوع پلاک",
                            "font_size": 11,
                            "line_height": 16,
                            "text_color": "#6b7280",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "match_parent"},
                            "height": {"type": "wrap_content"},
                            "margins": {"bottom": 2},
                        },
                        {
                            "type": "text",
                            "text": patch_text,
                            "font_size": 14,
                            "font_weight": "medium",
                            "line_height": 22,
                            "text_color": "#111827",
                            "text_alignment_horizontal": "right",
                            "width": {"type": "match_parent"},
                            "height": {"type": "wrap_content"},
                        },
                    ],
                },
            ],
        }
    )


def polish_inquiry_buttons(row):
    """Allow inquiry button text to wrap instead of clipping in fixed 48dp."""
    if not isinstance(row, dict):
        return
    if row.get("orientation") != "horizontal":
        return
    items = row.get("items") or []
    if len(items) < 2:
        return
    # second item is usually inquiry button with text___11
    for child in items:
        texts = []

        def collect(n):
            if isinstance(n, dict):
                if n.get("type") == "text" and n.get("text") == "@{text___11}":
                    texts.append(n)
                for v in n.get("items") or []:
                    collect(v)

        collect(child)
        if texts:
            child["height"] = {"type": "wrap_content"}
            child.setdefault("paddings", {})
            child["paddings"] = {
                "top": 12,
                "bottom": 12,
                "left": 12,
                "right": 12,
            }
            child["border"] = {"corner_radius": 8}
            for t in texts:
                t["line_height"] = 20
                t["font_weight"] = "medium"
                t["text_alignment_horizontal"] = "center"
                t["text_alignment_vertical"] = "center"


# Locate main content container: gallery -> items[0] -> items (header + body)
gallery = data["card"]["states"][0]["div"]
outer = gallery["items"][0]
# outer.items[0] is a container with header + body
main_col = outer["items"][0]
header = main_col["items"][0]
body = main_col["items"][1]

print("header type", header.get("type"), "n", len(header.get("items") or []))
print("body type", body.get("type"), "n", len(body.get("items") or []))

polish_header(header)

# body first child is plate type etc.
body_items = body.get("items") or []
if body_items:
    # plate type is typically first
    polish_plate_type(body_items[0])

# find inquiry button row: horizontal with two children including text___11
for it in body_items:
    s = json.dumps(it, ensure_ascii=False)
    if "text___11" in s and it.get("orientation") == "horizontal":
        polish_inquiry_buttons(it)
        print("polished inquiry row")

for path, node in find_by_visibility(data, "variable_car_info_visibility"):
    # only polish the main card (large fixed height / overlap)
    h = node.get("height") or {}
    if node.get("orientation") == "overlap" or (isinstance(h, dict) and h.get("value") == 283):
        polish_car_info(node)
        print("polished car info", path)
    elif "variable_car_model_information_vt" in json.dumps(node, ensure_ascii=False):
        # still the car card
        polish_car_info(node)
        print("polished car info(alt)", path)

# Fix other fixed-height value texts that clip (height 15 / 24 on dynamic values)
changed = 0

def fix_clipping_texts(n):
    global changed
    if isinstance(n, dict):
        if n.get("type") == "text":
            h = n.get("height")
            txt = str(n.get("text", ""))
            if isinstance(h, dict) and h.get("type") == "fixed" and h.get("value") in (15, 24):
                if "variable_" in txt or "@{" in txt:
                    n["height"] = {"type": "wrap_content"}
                    n["line_height"] = max(int(n.get("line_height") or 0), 20)
                    changed += 1
        for v in n.values():
            fix_clipping_texts(v)
    elif isinstance(n, list):
        for v in n:
            fix_clipping_texts(v)

fix_clipping_texts(data)
print("fixed clipping texts", changed)

with open(p, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)
    f.write("\n")
print("saved")
