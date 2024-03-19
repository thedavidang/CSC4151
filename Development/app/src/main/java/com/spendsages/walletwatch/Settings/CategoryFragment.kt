package com.spendsages.walletwatch

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.spendsages.walletwatch.databinding.FragmentCategoryBinding
import org.w3c.dom.Document
import java.io.OutputStreamWriter
import javax.xml.parsers.DocumentBuilderFactory

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment] constructor method to
 * create an instance of this fragment.
 */
class CategoryFragment : Fragment() {
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var settings : SettingsActivity
    private lateinit var model : SharedViewModel
    private lateinit var archive : Document

    private var categories = arrayOfNulls<String?>(3)
    private var categoryInputs = arrayOfNulls<String?>(3)

    /* Array that holds the new label for each category that the user changes.
    * If there is no change to a category, that index MUST be null. */
    private var changed = arrayOfNulls<String?>(3)

    private lateinit var saveButton : Button

    private lateinit var exportButton : Button
    private lateinit var importButton : Button

    private val categoryTextboxes = Array<TextInputEditText?>(3) { null }

    private lateinit var success : Toast

    /* Purpose: Controller method that disables and greys-out Save Changes button or
    * enables and reveals the Save Changes button.
    *
    * Parameters: enable represents a Boolean of whether or not
    *             to enable the Save Changes button.
    *
    * Returns: Nothing. */
    private fun toggleSaveButton(enable : Boolean) {
        /* Only enable if not already enabled. */
        if (enable && !saveButton.isEnabled) {
            saveButton.isEnabled = true
            saveButton.isClickable = true
            /* Set opacity to 100 % */
            saveButton.alpha = 1.0F
        }
        /* Only disable if not already disabled. */
        else if (!enable && saveButton.isEnabled) {
            saveButton.isEnabled = false
            saveButton.isClickable = false
            /* Set opacity to 50 % */
            saveButton.alpha = 0.5F
        }
    }

    /* Purpose: Controller method that checks if the user entered in
    * at least one truly new category label, but with no duplicated labels.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    private fun checkInputs() {
        /* Reset changed array. */
        changed[0] = ""
        changed[1] = ""
        changed[2] = ""

        /* Grab user input values. */
        for ((index, categoryTextbox) in categoryTextboxes.withIndex()) {
            categoryInputs[index] = categoryTextbox!!.text.toString().split(
                " ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        }

        /* Check for duplicate inputs or if there are no new categories. */
        val duplicatesFound = (categoryInputs.groupingBy { it }.eachCount()
            .filter { it.value > 1 }).isNotEmpty()
        val noNewLabels = categoryInputs.sortedBy { it } == categories.sortedBy { it }

        if (duplicatesFound || noNewLabels) {
            toggleSaveButton(false)
            return
        }

        /* Make temporary, mutable copy of user inputs. */
        val tempCategoryInputs = categoryInputs.clone()

        /* Set any existing categories to null in their same position as to ignore reordering. */
        for ((index, category) in categories.withIndex()) {
            if (category in categoryInputs) {
                changed[index] = null
                tempCategoryInputs[categoryInputs.indexOf(category)] = null
            }
        }

        /* Place the new category label(s) in any open slots,
        * preferring the textbox index if possible */
        for ((index, category) in tempCategoryInputs.withIndex()) {
            if (category != null) {
                if (changed[index] == "") {
                    /* Forcibly capitalize each word in new category label. */
                    changed[index] = category.split(
                        " ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                }
                else {
                    changed[changed.indexOf("")] = category
                }
            }
        }

        /* Enable Save Changes button. */
        toggleSaveButton(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val rootView = binding.root
        settings = activity as SettingsActivity
        model = settings.model

        /* Grab the labels of the categories as they currently are in the XML data file. */
        categories = DataManager.getCategories(model.get()).slice(1..3).toTypedArray()

        saveButton = rootView.findViewById(R.id.saveButton)
        /* Disable and grey-out the Save Changes button,
        * since the user has not made any changes yet. */
        toggleSaveButton(false)

        /* This listener for the Save Change button saves the changed category,
        * displays the Toast message, and disables the Save Changes button. */
        saveButton.setOnClickListener {
            val dialogClickListener: DialogInterface.OnClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        /* If user taps "Yes", then call the back-end function. */
                        DialogInterface.BUTTON_POSITIVE -> {
                            val doc = model.get()
                            /* A category was changed without any restoration. */
                            success = if (
                                DataManager.changeCategories(settings, doc, archive, changed)) {
                                Toast.makeText(
                                    context, R.string.changedCategoryString, Toast.LENGTH_LONG)
                            }
                            /* A category was restored from the Archive.xml. */
                            else {
                                Toast.makeText(
                                    context, R.string.restoredCategoryString, Toast.LENGTH_LONG)
                            }

                            model.save()
                            success.show()
                            toggleSaveButton(false)
                            /* Retrieve updated categories. */
                            categories = DataManager.getCategories(
                                doc).slice(1..3).toTypedArray()
                        }
                        /* If the user taps "No", then simply close the confirmation alert. */
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            /* Open Archive XML file. */
            archive = DataManager.getArchive(settings)

            val archivedLabels = DataManager.getArchivedCategories(archive)
            val newLabels = mutableListOf<String?>()
            val restoredLabels = mutableListOf<String?>()

            /* Create the message to display on the confirmation alert. */
            var message = "The following categories will stop being tracked " +
                    "and will be stored in an archive:\n"
            /* Output list of categories to stop tracking. */
            var index = 0
            while (index < 3) {
                if (changed[index] != null) {
                    message += "- " + categories[index] + "\n"

                    if (changed[index] in archivedLabels) {
                        restoredLabels.add("- " + changed[index] + "\n")
                    }
                    else {
                        newLabels.add("- " + changed[index] + "\n")
                    }
                }
                index++
            }

            /* Output list of new categories to start tracking. */
            if (newLabels.size > 0) {
                message += "\nThe following new categories will start being tracked:\n"
                for (label in newLabels) {
                    message += label
                }
            }

            /* Output list of restored categories to start tracking. */
            if (restoredLabels.size > 0) {
                message += "\nThe following archived categories will " +
                        "be restored and resume being tracked:\n"
                for (label in restoredLabels) {
                    message += label
                }
            }

            message += "\nAre you sure you want to make these changes?"
            /* Display the confirmation alert. */
            builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        /* Populate the array of category textboxes. */
        categoryTextboxes[0] = rootView.findViewById(R.id.category1Edit)
        categoryTextboxes[1] = rootView.findViewById(R.id.category2Edit)
        categoryTextboxes[2] = rootView.findViewById(R.id.category3Edit)

        /* Iterate through each category textbox. */
        for ((index, textbox) in categoryTextboxes.withIndex()) {
            /* Set the label for each category textbox
            as they currently are in the XML data file. */
            textbox!!.setText(categories[index])
            categoryInputs[index] = categories[index]

            /* Listener that checks if the user changed the category textbox content. */
            textbox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                    /* Remove all whitespace from user input in category textbox. */
                    val trimmed = s.toString().trim { it <= ' ' }
                    if (s.toString() != trimmed) {
                        /* Temporarily disable this text change listener to
                        * prevent multiple triggering events be fired. */
                        textbox.removeTextChangedListener(this)
                        /* Forcibly update the text displayed in the textbox. */
                        textbox.setText(trimmed)
                        /* Set cursor position. */
                        textbox.setSelection(trimmed.length)
                        /* Restore the text change listener. */
                        textbox.addTextChangedListener(this)
                    }

                    /* Check if the user did not enter any word(s) into the category textbox. */
                    if (trimmed.isEmpty()) {
                        /* Disable and grey-out the Save Changes button. */
                        toggleSaveButton(false)
                    }
                    else {
                        /* Check if at least one new category has been entered.
                        * If so, the Save Changes button will be enabled.*/
                        checkInputs()
                    }
                }
            })
        }

        exportButton = rootView.findViewById(R.id.exportButton)
        /* Listener that copies content of XML file to the clipboard. */
        exportButton.setOnClickListener {
            /* Present dialog to user for selecting which XML file to copy. */
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setTitle("Backup")
                setMessage("Which data set do you want to copy?\n\n" +
                        "\"Active\" corresponds to the data stored for the current " +
                        "categories.\n\n" +
                        "\"Inactive\" corresponds to the data stored for archived categories.")
                setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton("Active") { _, _ ->
                    /* Grab the text content of the XML data file. */
                    val text: CharSequence = model.getString(model.get())
                    /* Get a handle to the system clipboard service. */
                    val clipboardManager = getSystemService(context, ClipboardManager::class.java)
                    /* Create a ClipData object to hold the data to be copied. */
                    val clipData = ClipData.newPlainText(
                        getString(R.string.docFilenameString), text)
                    /* Set the data to the clipboard. */
                    clipboardManager?.setPrimaryClip(clipData)
                    /* Let user know that copy succeeded. */
                    Toast.makeText(context,
                        "Active data copied to clipboard", Toast.LENGTH_LONG).show()
                }
                setPositiveButton("Inactive") { _, _ ->
                    /* Open Archive XML file. */
                    archive = DataManager.getArchive(settings)
                    /* Grab the text content of the XML archive file. */
                    val text: CharSequence = model.getString(archive)
                    /* Get a handle to the system clipboard service. */
                    val clipboardManager = getSystemService(context, ClipboardManager::class.java)
                    /* Create a ClipData object to hold the data to be copied. */
                    val clipData = ClipData.newPlainText(
                        getString(R.string.docFilenameString), text)
                    /* Set the data to the clipboard. */
                    clipboardManager?.setPrimaryClip(clipData)
                    /* Let user know that copy succeeded. */
                    Toast.makeText(context,
                        "Inactive data copied to clipboard", Toast.LENGTH_LONG).show()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        importButton = rootView.findViewById(R.id.importButton)
        /* Listener that lets user add content to Archive XML file. */
        importButton.setOnClickListener {
            /* Open Archive XML file. */
            archive = DataManager.getArchive(settings)
            /* Grab the text content of the XML archive file. */
            val text: CharSequence = model.getString(archive)
            /* Populate textbox with content of the XML archive file. */
            val editText = EditText(context)
            editText.setText(text)

            /* Present dialog to user for editing the date XML file to copy. */
            val builder = AlertDialog.Builder(context)
            builder.apply {
                setTitle("Edit Archived Data of Inactive Categories")
                setView(editText)
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Save") { _, _ ->
                    val confirmation = AlertDialog.Builder(context)
                    confirmation.apply {
                        setTitle("Confirm")
                        setMessage("Are you sure you want to overwrite the archived data?\n\n" +
                                "This cannot be undone.")
                        setNegativeButton("Yes") { _, _ ->
                            /* Grab the user's input. */
                            val inputText = editText.text.toString()

                            /* Validate the user's text input by first saving to a temp file. */
                            val tempWriter = OutputStreamWriter(settings.openFileOutput(
                                "TEMP.XML", Context.MODE_PRIVATE))
                            tempWriter.write(inputText)
                            tempWriter.close()
                            /* Open the newly created TEMP XML file. */
                            val tempFile = settings.getFileStreamPath("TEMP.XML")

                            try {
                                /* Attempt to read the TEMP XML file into a Document object. */
                                val tempDoc = DocumentBuilderFactory.newInstance()
                                    .newDocumentBuilder().parse(tempFile)
                                if (tempDoc.getElementById("r") == null) {
                                    throw Exception("No root id 'r' was found.")
                                }
                                /* Clean up TEMP XML file. */
                                tempFile.delete()

                                /* Overwrite the Archive XML file with the user's text input. */
                                val outputWriter = OutputStreamWriter(settings.openFileOutput(
                                    settings.getString(R.string.archiveFilenameString),
                                    Context.MODE_PRIVATE))
                                outputWriter.write(inputText)
                                outputWriter.close()

                                /* Let user know that restore succeeded. */
                                Toast.makeText(context, "Saved changes to archived data",
                                    Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                /* Let user know that restore failed. */
                                Toast.makeText(context,
                                    "ERROR: Invalid input! Please try again.",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                        setPositiveButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    val confirm = confirmation.create()
                    confirm.show()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }

        rootView.setOnTouchListener { _: View, _: MotionEvent ->
            /* Hide the keyboard. */
            (settings.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(categoryTextboxes[0]!!.windowToken, 0)
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}