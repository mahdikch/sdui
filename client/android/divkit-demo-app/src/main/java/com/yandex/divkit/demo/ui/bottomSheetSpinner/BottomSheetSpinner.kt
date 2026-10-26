package com.yandex.divkit.demo.ui.bottomSheetSpinner

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yandex.div.core.DivViewFacade
import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.remote.PhPlusApi
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import com.yandex.divkit.demo.databinding.BottomSheetSpinnerBinding
import com.yandex.divkit.demo.ui.LoadScreenListener
import com.yandex.divkit.demo.utils.SingletonObjects
import org.json.JSONArray
import java.lang.ref.WeakReference
import java.util.HashMap

class BottomSheetSpinner(
    private val context: Context,
    private val listener: AdapterBottomSheetSpinner.CustomItemListener,
    private val key: String,
    private val title: String,
    private val codeVar: String,
    private val titleVar: String,
    private val view: DivViewFacade,
    private val map: MutableMap<String, String> = HashMap(),
    private val loadScreenListener: LoadScreenListener? = null,
) : BottomSheetDialogFragment() {

    lateinit var dialog: BottomSheetDialog
    lateinit var rootview: View
    private var _binding: BottomSheetSpinnerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdapterBottomSheetSpinner
    private lateinit var viewModelBottomSheetSpinner: ViewModelBottomSheetSpinner
    private lateinit var repository: PhPlusRepository
    private lateinit var remoteDataSource: PhPlusRemoteDataSource
    private lateinit var sharePref: SharePref

    private var currentPage = 1
    private var currentSearch = ""
    private var isLoadingPage = false
    private var hasMorePages = true
    private var pagedMode = false
    private var pendingRequestSeq = 0
    private val loadedItems = ArrayList<ListItemDto>()
    private var bindVarName: String = ""
    private var bindTitleVar: String = ""
    private var bindCodeVar: String = ""
    private var bindSysName: String? = null
    private var bindPath: String? = null

    companion object {
        private val SHEET_INTERNAL_KEYS = setOf(
            "id", "title", "code_var", "title_var", "varName",
            "sysName", "path", "selection", "selected_codes",
            "sync_from", "sync_fields", "sync_match", "sync_nested",
            "SEARCHABLE", "searchable",
            "service_path", "response_key", "id_field", "name_field",
            "search_param", "page_param", "paged", "has_more_field"
        )

        @Volatile
        private var pendingSheet: WeakReference<BottomSheetSpinner>? = null

        fun deliverServiceResponse(data: Map<String, String>): Boolean {
            val sheet = pendingSheet?.get() ?: return false
            if (!sheet.isAdded || !sheet.pagedMode) return false
            sheet.applyServiceResponse(data)
            return true
        }

        fun deliverServiceError(message: String?) {
            val sheet = pendingSheet?.get() ?: return
            if (!sheet.isAdded || !sheet.pagedMode) return
            sheet.applyServiceError(message)
        }

        private fun register(sheet: BottomSheetSpinner) {
            pendingSheet = WeakReference(sheet)
        }

        private fun unregister(sheet: BottomSheetSpinner) {
            val current = pendingSheet?.get()
            if (current == null || current === sheet) {
                pendingSheet = null
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val sheetHeight = (requireContext().resources.displayMetrics.heightPixels * 70) / 100
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            if (_binding != null) {
                binding.root.minimumHeight = sheetHeight
                binding.root.layoutParams = (binding.root.layoutParams
                    ?: ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        sheetHeight
                    )).apply {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = sheetHeight
                }
            }
            bottomSheet?.requestLayout()
            dialog.behavior.apply {
                skipCollapsed = true
                isFitToContents = true
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSpinnerBinding.inflate(inflater, container, false)
        val retrofit = SingletonObjects.retrofitInstance()
        val create = retrofit?.create(PhPlusApi::class.java)
        val database = SingletonObjects.getDbInstance(context)
        val sharedPreferences = SingletonObjects.sharedPreferencesInstance(context)
        if (sharedPreferences != null)
            sharePref = SharePref(sharedPreferences)
        if (create != null)
            remoteDataSource = PhPlusRemoteDataSource(create)
        if (database != null && ::remoteDataSource.isInitialized && ::sharePref.isInitialized) {
            repository = PhPlusRepository(remoteDataSource, sharePref, database.phPlusDBDao())
            val factory = MyViewModelFactory(repository)
            viewModelBottomSheetSpinner =
                ViewModelProvider(this, factory)[ViewModelBottomSheetSpinner::class.java]
        }

        setUp()
        val sheetHeight = (resources.displayMetrics.heightPixels * 70) / 100
        binding.root.minimumHeight = sheetHeight
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            sheetHeight
        )

        return binding.root
    }

    override fun onDestroyView() {
        unregister(this)
        _binding = null
        super.onDestroyView()
    }

    private fun resolvedServicePath(): String {
        val raw = map["service_path"].orEmpty().ifBlank { map["path"].orEmpty() }
        return Uri.decode(raw).trim()
    }

    private fun setUp() {
        binding.title.text = title
        adapter = AdapterBottomSheetSpinner(listener)
        bindVarName = map["varName"].orEmpty().ifBlank { key }
        bindPath = map["path"]
        bindSysName = map["sysName"]
        bindCodeVar = codeVar
        bindTitleVar = titleVar
        val selectionMode = map["selection"] ?: "single"
        val isMulti = selectionMode.equals("multi", ignoreCase = true)
        Log.d(
            "BottomSheetSpinner",
            "setUp: selectionMode=$selectionMode isMulti=$isMulti key=$key map=$map servicePath=${resolvedServicePath()}"
        )
        adapter.setMultiSelect(isMulti)

        val servicePath = resolvedServicePath()
        // Any non-blank service_path means remote paged/searchable list.
        pagedMode = servicePath.isNotBlank() &&
            (map["paged"].equals("true", ignoreCase = true) ||
                map.containsKey("page_param") ||
                map.containsKey("search_param") ||
                map.containsKey("service_path"))

        val gridLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = gridLayoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.hasFixedSize()

        if (pagedMode) {
            setupPagedServiceMode(isMulti)
        } else {
            binding.btnSearch.visibility = View.GONE
            setupLocalDbMode(isMulti)
        }
    }

    private fun setupLocalDbMode(isMulti: Boolean) {
        val arrayList: ArrayList<ListItemDto> = arrayListOf()
        try {
            if (!::viewModelBottomSheetSpinner.isInitialized) {
                Toast.makeText(requireContext(), "خطا در بارگذاری لیست", Toast.LENGTH_SHORT).show()
                return
            }
            val json: PhPlusDB = viewModelBottomSheetSpinner.getValueByKey(key)
            val turnsType = object : TypeToken<List<ListItemDto>>() {}.type
            val items: List<ListItemDto> = try {
                Gson().fromJson<List<ListItemDto>>(json.value, turnsType) ?: emptyList()
            } catch (e: Exception) {
                Log.e("BottomSheetSpinner", "Failed to parse DB list for key=$key", e)
                emptyList()
            }
            arrayList.addAll(items)
            for (item in items) {
                for (constraint in item.constraintList.orEmpty()) {
                    if (constraint.variableName == "usage.res" &&
                        map.contains("usage") &&
                        constraint.disallowedList.contains(map["usage"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "delivery.res" &&
                        map.contains("delivery") &&
                        constraint.disallowedList.contains(map["delivery"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "plate.res" &&
                        map.contains("plate") &&
                        constraint.disallowedList.contains(map["plate"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "violation.res") {
                        if (map.contains("violation1") && constraint.disallowedList.contains(map["violation1"])) arrayList.remove(item)
                        if (map.contains("violation2") && constraint.disallowedList.contains(map["violation2"])) arrayList.remove(item)
                        if (map.contains("violation3") && constraint.disallowedList.contains(map["violation3"])) arrayList.remove(item)
                    }
                    if (constraint.variableName == "system.res" &&
                        map.contains("system") &&
                        constraint.disallowedList.contains(map["system"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "violations.res" &&
                        map.contains("violations") &&
                        constraint.disallowedList.contains(map["violations"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "city.res" &&
                        map.contains("city") &&
                        constraint.disallowedList.contains(map["city"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "province.res" &&
                        map.contains("province") &&
                        constraint.disallowedList.contains(map["province"])
                    ) arrayList.remove(item)
                    if (constraint.variableName == "category.res" &&
                        map.contains("category") &&
                        constraint.disallowedList.contains(map["category"])
                    ) arrayList.remove(item)
                }
            }
        } catch (e: Exception) {
            Log.e("BottomSheetSpinner", "DB list load failed for key=$key", e)
            Toast.makeText(requireContext(), "لیست خالی است", Toast.LENGTH_SHORT).show()
        }

        adapter.addItem(ArrayList(arrayList), bindVarName, bindTitleVar, bindCodeVar, bindSysName, bindPath, map)

        if (isMulti) {
            var selectedCodesCsv = map["selected_codes"]
            if (selectedCodesCsv.isNullOrEmpty() && ::viewModelBottomSheetSpinner.isInitialized) {
                try {
                    val currentValue = viewModelBottomSheetSpinner.getValueByKey(codeVar)?.value
                    if (!currentValue.isNullOrBlank() && currentValue != "null") {
                        selectedCodesCsv = currentValue
                    }
                } catch (_: Exception) {
                }
            }
            val preselected =
                selectedCodesCsv?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?: emptyList()
            adapter.setPreselected(preselected)
            binding.actions.visibility = View.VISIBLE
            binding.btnDone.visibility = View.VISIBLE
            binding.btnDone.setOnClickListener {
                val selected = adapter.getSelectedItems()
                val codes = selected.joinToString(",") { it.id }
                val titles = selected.joinToString(",") { it.titleFa }
                val div2View = view as? com.yandex.div.core.view2.Div2View
                div2View?.setVariable(codeVar, codes)
                div2View?.setVariable(titleVar, titles)
                dismiss()
            }
        } else {
            binding.actions.visibility = View.GONE
        }

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                val filtered = arrayList.filter {
                    query.isEmpty() || it.titleFa.contains(query) || it.id.contains(query)
                }
                adapter.addItem(
                    ArrayList(filtered),
                    bindVarName,
                    bindTitleVar,
                    bindCodeVar,
                    bindSysName,
                    bindPath,
                    map
                )
            }
        })
    }

    private fun setupPagedServiceMode(isMulti: Boolean) {
        binding.actions.visibility = if (isMulti) View.VISIBLE else View.GONE
        binding.btnSearch.visibility = View.VISIBLE
        adapter.addItem(ArrayList(), bindVarName, bindTitleVar, bindCodeVar, bindSysName, bindPath, map)

        val runSearch = {
            currentSearch = binding.search.text?.toString().orEmpty().trim()
            currentPage = 1
            hasMorePages = true
            loadedItems.clear()
            fetchPagedPage(reset = true)
        }

        binding.btnSearch.setOnClickListener { runSearch() }
        binding.search.setOnEditorActionListener { _, actionId, event ->
            val isSearch = actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            if (isSearch) {
                runSearch()
                true
            } else {
                false
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || isLoadingPage || !hasMorePages) return
                val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (lm.findLastVisibleItemPosition() >= adapter.itemCount - 3) {
                    currentPage += 1
                    fetchPagedPage(reset = false)
                }
            }
        })

        currentPage = 1
        currentSearch = ""
        hasMorePages = true
        fetchPagedPage(reset = true)
    }

    private fun fetchPagedPage(reset: Boolean) {
        if (isLoadingPage) return
        if (!reset && !hasMorePages) return

        val servicePath = resolvedServicePath()
        if (servicePath.isBlank()) {
            Toast.makeText(requireContext(), "مسیر سرویس مشخص نیست", Toast.LENGTH_SHORT).show()
            return
        }
        if (loadScreenListener == null) {
            Toast.makeText(requireContext(), "امکان فراخوانی سرویس نیست", Toast.LENGTH_SHORT).show()
            Log.e("BottomSheetSpinner", "loadScreenListener is null")
            return
        }

        isLoadingPage = true
        binding.btnSearch.isEnabled = false

        val searchParam = map["search_param"].orEmpty().ifBlank { "name" }
        val pageParam = map["page_param"].orEmpty().ifBlank { "pageNumber" }

        val request = HashMap<String, String>()
        map.forEach { (k, v) ->
            if (!SHEET_INTERNAL_KEYS.contains(k)) {
                request[k] = v
            }
        }
        request["path"] = servicePath
        request[searchParam] = currentSearch
        request[pageParam] = currentPage.toString()
        if (request["sysName"].isNullOrBlank()) {
            request["sysName"] = bindSysName.orEmpty().ifBlank {
                dbValueOrEmpty("sysName")
            }
        }
        if (!request.containsKey("ph/token")) {
            request["ph/token"] = "empty"
        }

        pendingRequestSeq += 1
        register(this)

        Log.d(
            "BottomSheetSpinner",
            "fetchPagedPage reset=$reset page=$currentPage search='$currentSearch' path=$servicePath request=$request"
        )
        loadScreenListener.onRequest(request)
    }

    fun applyServiceResponse(data: Map<String, String>) {
        if (_binding == null || !isAdded) return
        isLoadingPage = false
        binding.btnSearch.isEnabled = true

        val responseKey = map["response_key"].orEmpty().ifBlank { "json" }
        val idField = map["id_field"].orEmpty().ifBlank { "id" }
        val nameField = map["name_field"].orEmpty().ifBlank { "name" }
        val hasMoreField = map["has_more_field"].orEmpty()

        val rawJson = data[responseKey].orEmpty()
        val pageItems = parseFlexibleListItems(rawJson, idField, nameField)
        hasMorePages = if (hasMoreField.isNotBlank()) {
            data[hasMoreField]?.equals("true", ignoreCase = true) == true
        } else {
            pageItems.isNotEmpty()
        }

        if (currentPage == 1) {
            loadedItems.clear()
            loadedItems.addAll(pageItems)
            adapter.addItem(
                ArrayList(loadedItems),
                bindVarName,
                bindTitleVar,
                bindCodeVar,
                bindSysName,
                bindPath,
                map
            )
        } else {
            val existing = loadedItems.map { it.id }.toHashSet()
            val toAdd = pageItems.filter { it.id.isNotBlank() && !existing.contains(it.id) }
            loadedItems.addAll(toAdd)
            adapter.appendItems(toAdd)
        }

        Log.d(
            "BottomSheetSpinner",
            "page applied size=${pageItems.size} total=${loadedItems.size} hasMore=$hasMorePages rawLen=${rawJson.length}"
        )
        if (pageItems.isEmpty() && currentPage == 1) {
            Toast.makeText(requireContext(), "موردی یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyServiceError(message: String?) {
        if (_binding == null || !isAdded) return
        isLoadingPage = false
        binding.btnSearch.isEnabled = true
        if (currentPage > 1) {
            currentPage -= 1
        }
        Toast.makeText(
            requireContext(),
            message ?: "خطا در دریافت لیست",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dbValueOrEmpty(keyName: String): String {
        if (!::viewModelBottomSheetSpinner.isInitialized) return ""
        return try {
            viewModelBottomSheetSpinner.getValueByKey(keyName)?.value.orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseFlexibleListItems(
        raw: String,
        idField: String,
        nameField: String,
    ): List<ListItemDto> {
        if (raw.isBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            val out = ArrayList<ListItemDto>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val id = firstNonBlank(
                    obj.optString(idField),
                    obj.optString("id"),
                    obj.optString("code")
                )
                val name = firstNonBlank(
                    obj.optString(nameField),
                    obj.optString("name"),
                    obj.optString("titleFa"),
                    obj.optString("title")
                )
                if (id.isBlank() && name.isBlank()) continue
                out.add(
                    ListItemDto(
                        id = id.ifBlank { name },
                        parentId = obj.optString("parentId", ""),
                        titleFa = name.ifBlank { id },
                        titleEn = obj.optString("titleEn", ""),
                        constraintList = emptyList()
                    )
                )
            }
            out
        } catch (e: Exception) {
            Log.e("BottomSheetSpinner", "parseFlexibleListItems failed rawLen=${raw.length}", e)
            emptyList()
        }
    }

    private fun firstNonBlank(vararg values: String): String {
        for (v in values) {
            if (v.isNotBlank() && v != "null") return v
        }
        return ""
    }
}

class MyViewModelFactory(private val repository: PhPlusRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelBottomSheetSpinner::class.java)) {
            return ViewModelBottomSheetSpinner(repository) as T
        }
        throw Exception("asd")
    }
}
