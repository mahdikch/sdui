package com.yandex.divkit.demo.div.faultDrivers

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import com.yandex.divkit.demo.utils.FaultDriverItem
import com.yandex.divkit.demo.utils.FaultDriversLoader
import com.yandex.divkit.demo.utils.VehicleEntryCountHelper

class FaultDriversListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mehdiViewModel: MehdiViewModel? = null
    private var accidentId: String = ""

    init {
        orientation = VERTICAL
        layoutDirection = LAYOUT_DIRECTION_RTL
        textDirection = TEXT_DIRECTION_RTL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun bind(viewModel: MehdiViewModel?, accidentId: String) {
        this.mehdiViewModel = viewModel
        this.accidentId = accidentId
        reload()
    }

    fun reload() {
        val resolvedAccidentId = VehicleEntryCountHelper.resolveAccidentId(mehdiViewModel, accidentId)
        val drivers = FaultDriversLoader.load(mehdiViewModel, resolvedAccidentId)
        android.util.Log.d("FaultDriversListView", "reload: accidentId=$resolvedAccidentId count=${drivers.size}")
        render(drivers)
    }

    private fun render(drivers: List<FaultDriverItem>) {
        removeAllViews()
        if (drivers.isEmpty()) {
            addView(emptyCard())
            return
        }
        drivers.forEachIndexed { index, driver ->
            addView(driverCard(driver, index + 1))
        }
    }

    private fun driverCard(driver: FaultDriverItem, index: Int): View {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutDirection = LAYOUT_DIRECTION_RTL
            textDirection = TEXT_DIRECTION_RTL
            gravity = Gravity.RIGHT
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10)
            }
            background = roundedStroke("#ffffff", "#c8d0e4", 8)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            elevation = dp(1).toFloat()
        }

        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutDirection = LAYOUT_DIRECTION_LTR
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(10)
            }
        }

        // LTR row: first child is left, last is right → add name then badge so badge is on the right.
        header.addView(
            TextView(context).apply {
                text = driver.driverName.ifBlank { "راننده مقصر" }
                setTextColor(Color.parseColor("#1f2937"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textDirection = TEXT_DIRECTION_RTL
                textAlignment = TEXT_ALIGNMENT_VIEW_END
                gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = dp(8)
                }
            },
        )
        header.addView(
            TextView(context).apply {
                text = "مقصر $index"
                setTextColor(Color.parseColor("#193d8d"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textDirection = TEXT_DIRECTION_RTL
                textAlignment = TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
                background = roundedSolid("#e8ecf4", 6)
                setPadding(dp(10), dp(4), dp(10), dp(4))
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            },
        )
        card.addView(header)

        card.addView(infoRow("علت", driver.cause.ifBlank { "—" }))
        card.addView(infoRow("وسیله", driver.vehicleModel.ifBlank { "—" }))
        card.addView(infoRow("پلاک", driver.plate.ifBlank { "—" }, last = true))
        return card
    }

    private fun infoRow(label: String, value: String, last: Boolean = false): View {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutDirection = LAYOUT_DIRECTION_LTR
            gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                if (!last) bottomMargin = dp(8)
            }
            background = roundedSolid("#f8fafc", 6)
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }

        // LTR: value (flex) then label → label sits on the right.
        row.addView(
            TextView(context).apply {
                text = value
                setTextColor(Color.parseColor("#1f2937"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                textDirection = TEXT_DIRECTION_RTL
                textAlignment = TEXT_ALIGNMENT_VIEW_END
                gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginEnd = dp(8)
                }
            },
        )
        row.addView(
            TextView(context).apply {
                text = label
                setTextColor(Color.parseColor("#6b7280"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                textDirection = TEXT_DIRECTION_RTL
                textAlignment = TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER
                background = roundedSolid("#ffffff", 4)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            },
        )
        return row
    }

    private fun emptyCard(): View {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            layoutDirection = LAYOUT_DIRECTION_RTL
            textDirection = TEXT_DIRECTION_RTL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            background = roundedStroke("#ffffff", "#c8d0e4", 8)
            setPadding(dp(16), dp(20), dp(16), dp(20))

            addView(
                TextView(context).apply {
                    text = "راننده مقصری ثبت نشده است"
                    setTextColor(Color.parseColor("#1f2937"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textDirection = TEXT_DIRECTION_RTL
                    textAlignment = TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = dp(4)
                    }
                },
            )
            addView(
                TextView(context).apply {
                    text = "در صفحه اطلاعات خودرو، وضعیت مقصر را انتخاب کنید."
                    setTextColor(Color.parseColor("#6b7280"))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    textDirection = TEXT_DIRECTION_RTL
                    textAlignment = TEXT_ALIGNMENT_CENTER
                    gravity = Gravity.CENTER
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                },
            )
        }
    }

    private fun roundedSolid(color: String, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun roundedStroke(fill: String, stroke: String, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.parseColor(fill))
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(1), Color.parseColor(stroke))
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()
    }
}
