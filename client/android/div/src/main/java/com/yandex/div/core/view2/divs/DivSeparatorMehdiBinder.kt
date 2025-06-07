package com.yandex.div.core.view2.divs

import android.view.Gravity
import com.yandex.div.R
import com.yandex.div.core.dagger.DivScope
import com.yandex.div.core.view2.BindingContext
import com.yandex.div.core.view2.DivViewBinder
import com.yandex.div.core.view2.divs.widgets.DivSeparatorMehdiView
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div.json.expressions.equalsToConstant
import com.yandex.div.json.expressions.isConstantOrNull
import com.yandex.div2.DivSeparator
import javax.inject.Inject

@DivScope
internal class DivSeparatorMehdiBinder @Inject constructor(
    private val baseBinder: DivBaseBinder
) : DivViewBinder<DivSeparator, DivSeparatorMehdiView> {

//    override fun bindView(context: BindingContext, view: DivSeparatorMehdiView, div: DivSeparator) {
//        val oldDiv = view.div
//        if (div === oldDiv) return
//
//        baseBinder.bindView(context, view, div, oldDiv)
//        view.applyDivActions(
//            context,
//            div.action,
//            div.actions,
//            div.longtapActions,
//            div.doubletapActions,
//            div.hoverStartActions,
//            div.hoverEndActions,
//            div.pressStartActions,
//            div.pressEndActions,
//            div.actionAnimation,
//            div.accessibility,
//        )
//
//        view.bindStyle(div.delimiterStyle, oldDiv?.delimiterStyle, context.expressionResolver)
//
//        view.setDividerHeightResource(R.dimen.div_separator_delimiter_height)
//        view.dividerGravity = Gravity.CENTER
//    }
//
//    private fun DivSeparatorMehdiView.bindStyle(
//        newStyle: DivSeparatorMehdi.DelimiterStyle?,
//        oldStyle: DivSeparatorMehdi.DelimiterStyle?,
//        resolver: ExpressionResolver
//    ) {
//        if (newStyle?.color.equalsToConstant(oldStyle?.color)
//            && newStyle?.orientation.equalsToConstant(oldStyle?.orientation)) {
//            return
//        }
//
//        applyStyle(newStyle, resolver)
//
//        if (newStyle?.color.isConstantOrNull() && newStyle?.orientation.isConstantOrNull() ) {
//            return
//        }
//
//        val callback = { _: Any -> applyStyle(newStyle, resolver) }
//        addSubscription(newStyle?.color?.observe(resolver, callback))
//        addSubscription(newStyle?.orientation?.observe(resolver, callback))
//    }
//
//    private fun DivSeparatorMehdiView.applyStyle(style: DivSeparatorMehdi.DelimiterStyle?, resolver: ExpressionResolver) {
//        if (style == null) {
//            dividerColor = DivSeparatorMehdiView.DEFAULT_DIVIDER_COLOR
//            isHorizontal = true
//        } else {
//            dividerColor = style.color.evaluate(resolver)
//            val orientation = style.orientation.evaluate(resolver)
//            isHorizontal = orientation == DivSeparatorMehdi.DelimiterStyle.Orientation.HORIZONTAL
//        }
//    }
}
