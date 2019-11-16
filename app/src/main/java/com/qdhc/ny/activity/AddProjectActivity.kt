package com.qdhc.ny.activity

import android.content.DialogInterface
import android.os.Handler
import android.support.v7.app.AlertDialog
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.SaveListener
import com.qdhc.ny.R
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.Project
import com.qdhc.ny.common.ProjectData
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_add_project.*
import kotlinx.android.synthetic.main.layout_title_theme.*

/**
 * 添加新的工程
 */
class AddProjectActivity : BaseActivity() {

    var areaId = 0

    override fun intiLayout(): Int {
        return R.layout.activity_add_project
    }

    override fun initView() {
        title_tv_title.text = "创建新工程"
    }

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }

        managerTv.setOnClickListener {
            var builder = AlertDialog.Builder(this);
            builder.setTitle("请选择工程的负责人");
            var items = arrayOfNulls<String>(ProjectData.getInstance().userInfos.size)

            ProjectData.getInstance().userInfos.forEachIndexed { index, user -> items[index] = user.nickName }

            // -1代表没有条目被选中
            builder.setSingleChoiceItems(items, -1, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    var user = ProjectData.getInstance().userInfos[which]
                    managerTv.setText(user.nickName)
                    managerTv.tag = user.objectId
                    dialog?.dismiss()
                }
            });
            builder.show()
        }

        bt_add.setOnClickListener {
            var name = nameTv.text.toString().trim()
            if (name.isEmpty()) {
                ToastUtil.show(this, "工程名称不能为空")
                return@setOnClickListener
            }

            var content = edt_work.text.toString().trim()
            if (content.isEmpty()) {
                ToastUtil.show(this, "工程简介不能为空")
                return@setOnClickListener
            }

            var person = managerTv.text.toString().trim()
            if (person.isEmpty()) {
                ToastUtil.show(this, "请指定工程的负责人")
                return@setOnClickListener
            }

            showDialog("正在创建工程...")

            var project = Project()
            project.name = name
            project.introduce = content
            project.area = areaId
            project.manager = managerTv.tag.toString()

            project.save(object : SaveListener<String>() {
                override fun done(objectId: String?, e: BmobException?) {
                    if (e == null) {
                        showDialog("工程创建成功...")
                        Handler().postDelayed({
                            dismissDialogNow()
                            finish()
                        }, 1500)
                    } else {
                        showDialog("工程失败...")
                        dismissDialog()
                    }
                }
            })

        }
    }

    override fun initData() {

        areaId = intent.getIntExtra("area", 0)

    }

}
