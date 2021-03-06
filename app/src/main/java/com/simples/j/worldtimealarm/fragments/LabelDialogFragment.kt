package com.simples.j.worldtimealarm.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.simples.j.worldtimealarm.R

class LabelDialogFragment: DialogFragment() {

    private var listener: OnDialogEventListener? = null
    private var lastLabel: String = ""
    private var currentLabel: String = ""

    private lateinit var labelEditor: EditText
    private lateinit var fragmentContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.fragmentContext = context
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(savedInstanceState != null) {
            currentLabel = savedInstanceState.getString(CURRENT_LABEL, "")
        }

        val labelView = View.inflate(fragmentContext, R.layout.label_dialog_view, null)
        labelEditor = labelView.findViewById(R.id.label)
        labelEditor.setText(lastLabel)
        labelEditor.setTextColor(ContextCompat.getColor(fragmentContext, R.color.colorPrimary))
        if(lastLabel.isNotEmpty()) {
            labelEditor.selectAll()
        }
        labelEditor.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable) {
                currentLabel = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        val dialog = AlertDialog.Builder(context!!)
                .setTitle(resources.getString(R.string.label))
                .setView(labelView)
                .setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, _ ->
                    listener?.onPositiveButtonClick(dialogInterface, currentLabel)
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                    listener?.onNegativeButtonClick(dialogInterface)
                }
                .setNeutralButton(resources.getString(R.string.clear)) { dialogInterface, _ ->
                    currentLabel = ""
                    labelEditor.setText("")
                    listener?.onNeutralButtonClick(dialogInterface)
                }
                .setOnCancelListener { currentLabel = lastLabel }

        return dialog.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(CURRENT_LABEL, labelEditor.text.toString())
    }

    fun setOnDialogEventListener(listener: OnDialogEventListener) {
        this.listener = listener
    }

    fun setLastLabel(label: String) {
        this.lastLabel = label
        this.currentLabel = label
    }

    interface OnDialogEventListener {
        fun onPositiveButtonClick(inter: DialogInterface, label: String)
        fun onNegativeButtonClick(inter: DialogInterface)
        fun onNeutralButtonClick(inter: DialogInterface)
    }

    companion object {
        fun newInstance() = LabelDialogFragment()

        const val CURRENT_LABEL = "CURRENT_LABEL"
    }
}