package com.yandex.divkit.demo.ui.bottomSheetPlate

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.databinding.BottomSheetPlateBinding
import com.yandex.divkit.demo.databinding.BottomSheetSpinnerBinding
//import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


//@AndroidEntryPoint
class BottomSheetPlate(
    private var plateItemListener: PlateItemListener
) :
    BottomSheetDialogFragment() {
    lateinit var dialog: BottomSheetDialog
    lateinit var rootview: View
    private var _binding: BottomSheetPlateBinding? = null
    private val binding get() = _binding!!

    interface PlateItemListener {
        fun onClicked(plate: String,color:String,textColor:String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val metrics = requireContext().resources.displayMetrics
        val height = (metrics.heightPixels) * 60 / 100
        val width = metrics.heightPixels
//        dialog.behavior.peekHeight = height * 60 / 100
        dialog.window?.setLayout(width, height)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetPlateBinding.inflate(inflater, container, false)

        setUp()
        return binding.root
    }

    private fun setUp() {
        binding.alef.setOnClickListener {
            plateItemListener.onClicked("الف","#F21829","#FFFFFF")
            dialog.dismiss()
        }
        binding.pe.setOnClickListener {
            plateItemListener.onClicked("پ","#02532A","#FFFFFF")
            dialog.dismiss()
        }
        binding.te.setOnClickListener {
            plateItemListener.onClicked("ت","#FDCA00","#4F4F4F")
            dialog.dismiss()
        }
        binding.be.setOnClickListener {
            plateItemListener.onClicked("ب","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.je.setOnClickListener {
            plateItemListener.onClicked("ج","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.de.setOnClickListener {
            plateItemListener.onClicked("د","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.se.setOnClickListener {
            plateItemListener.onClicked("ث","#015428","#FFFFFF")
            dialog.dismiss()
        }
        binding.ze.setOnClickListener {
            plateItemListener.onClicked("ز","#016C80","#FFFFFF")
            dialog.dismiss()
        }
        binding.she.setOnClickListener {
            plateItemListener.onClicked("ش","#D6B481","#4F4F4F")
            dialog.dismiss()
        }
        binding.se.setOnClickListener {
            plateItemListener.onClicked("س","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.sad.setOnClickListener {
            plateItemListener.onClicked("ص","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.ta.setOnClickListener {
            plateItemListener.onClicked("ط","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.eyn.setOnClickListener {
            plateItemListener.onClicked("ع","#FDCA00","#4F4F4F")
            dialog.dismiss()
        }
        binding.fe.setOnClickListener {
            plateItemListener.onClicked("ف","#1966C9","#FFFFFF")
            dialog.dismiss()
        }
        binding.ke.setOnClickListener {
            plateItemListener.onClicked("ک","#F8D700","#4F4F4F")
            dialog.dismiss()
        }
        binding.ghaf.setOnClickListener {
            plateItemListener.onClicked("ق","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.lam.setOnClickListener {
            plateItemListener.onClicked("ل","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.mim.setOnClickListener {
            plateItemListener.onClicked("م","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.di.setOnClickListener {
            plateItemListener.onClicked("D","#AEC7FF","#4F4F4F")
            dialog.dismiss()
        }
        binding.zhe.setOnClickListener {
            plateItemListener.onClicked("ژ","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.es.setOnClickListener {
            plateItemListener.onClicked("S", "#AEC1FD","#4F4F4F")
            dialog.dismiss()
        }
        binding.non.setOnClickListener {
            plateItemListener.onClicked("ن","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.vav.setOnClickListener {
            plateItemListener.onClicked("و","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.he.setOnClickListener {
            plateItemListener.onClicked("ه","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }
        binding.ye.setOnClickListener {
            plateItemListener.onClicked("ی","#FFFFFF","#4F4F4F")
            dialog.dismiss()
        }


    }

}

//    override fun onClicked(item: ListItemDto) {
//        CustomBottomSheetSpinnerClickListener.value.value = item
//        dialog.dismiss()
//    }
