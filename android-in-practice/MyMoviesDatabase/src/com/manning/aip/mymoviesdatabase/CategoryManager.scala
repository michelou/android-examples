package com.manning.aip.mymoviesdatabase

import android.app.{Activity, AlertDialog, Dialog}
import android.content.DialogInterface
import android.os.Bundle
import android.view.{ContextMenu, MenuItem, View}
import android.view.ContextMenu.ContextMenuInfo
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, EditText, ListView}
import android.widget.TextView
import android.widget.Toast
import android.widget.AdapterView.AdapterContextMenuInfo

import scala.collection.JavaConversions._

import model.Category

import java.util.{Collections => JCollections, List => JList}

class CategoryManager extends Activity {

  private var app: MyMoviesApp = _

  private var categories: JList[Category] = _
  private var adapter: ArrayAdapter[Category] = _

  private var listView: ListView = _

  private var categoryAddShowDialog: Button = _
  private var categoryAddDialog: Dialog = _
  private var categoryAdd: EditText = _
  private var categoryAddSubmit: Button = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.category_manager)

    app = getApplication.asInstanceOf[MyMoviesApp]

    categories = app.getDataManager.getAllCategories
    listView = findViewById(R.id.category_manager_list).asInstanceOf[ListView]
    listView setEmptyView findViewById(R.id.category_manager_list_empty)
    adapter = new ArrayAdapter[Category](this, android.R.layout.simple_list_item_1, categories);
    listView setAdapter adapter
    registerForContextMenu(listView)

    categoryAddShowDialog = findViewById(R.id.category_add_show_dialog_button).asInstanceOf[Button]
    categoryAddShowDialog setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        if (!categoryAddDialog.isShowing) categoryAddDialog.show()
      }
    }

    categoryAddDialog = new Dialog(this)
    categoryAddDialog setContentView R.layout.category_add_dialog
    categoryAddDialog setTitle "   Add New Category"
    categoryAdd = categoryAddDialog.findViewById(R.id.category_add).asInstanceOf[EditText]
    categoryAddSubmit = categoryAddDialog.findViewById(R.id.category_add_submit).asInstanceOf[Button]
    categoryAddSubmit setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        if (!isTextViewEmpty(categoryAdd)) {
          val exists = app.getDataManager findCategory categoryAdd.getText.toString
          if (exists == null) {
            val category = new Category(0, categoryAdd.getText().toString());
            app.getDataManager saveCategory category
            // we could just ADD to adapter, and not backing collection
            // but that will put element at end of ListView, here we want to add and sort
            categories add category
            JCollections.sort(categories)
            adapter.notifyDataSetChanged()
          } else {
            Toast.makeText(CategoryManager.this, "Category already exists", Toast.LENGTH_SHORT).show()
          }
        }
        // cancel vs dismiss vs hide
        categoryAddDialog.cancel()
      }
    }
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo)
    menu.add(0, CategoryManager.CONTEXT_MENU_DELETE, 0, "Delete Category")
    menu setHeaderTitle "Action"
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
    val category = categories get info.position
    item.getItemId match {
      case CategoryManager.CONTEXT_MENU_DELETE =>
        new AlertDialog.Builder(CategoryManager.this)
          .setTitle("Delete Category?")
          .setMessage(category.getName)
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            def onClick(d: DialogInterface, i: Int) {
              app.getDataManager deleteCategory category
              adapter remove category
            }
          })
          .setNegativeButton("No", new DialogInterface.OnClickListener() {
            def onClick(d: DialogInterface, i: Int) {}
          }).show()
        true
      case _ =>
        super.onContextItemSelected(item)
    }
  }

  private def isTextViewEmpty(textView: TextView): Boolean =
    !((textView != null) && (textView.getText != null) &&
      (textView.getText.toString != null) && !textView.getText.toString.equals(""))
}

object CategoryManager {
  private final val CONTEXT_MENU_DELETE = 0
}
