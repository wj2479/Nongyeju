package com.qdhc.ny.activity

import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import com.qdhc.ny.R
import com.qdhc.ny.base.BaseActivity
import com.qdhc.ny.bmob.Notify
import com.qdhc.ny.bmob.NotifyReceiver
import com.qdhc.ny.bmob.UserInfo
import com.qdhc.ny.common.ProjectData
import com.qdhc.ny.treeview.MyNodeViewFactory
import com.sj.core.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_notify_publish.*
import kotlinx.android.synthetic.main.layout_title_theme.*
import me.texy.treeview.TreeNode
import me.texy.treeview.TreeView
import me.texy.treeview.helper.TreeHelper


/**
 * 发布通知
 */
class NotifyPublishActivity : BaseActivity() {

    lateinit var treeView: TreeView
    lateinit var user: UserInfo
    lateinit var root: TreeNode

    override fun intiLayout(): Int {
        return com.qdhc.ny.R.layout.activity_notify_publish
    }

    override fun initView() {
        title_tv_title.text = "发布通知"
        root = TreeNode.root()
    }

    var MAX_COUNT = 0
    var count = 0

    override fun initClick() {
        title_iv_back.setOnClickListener { finish() }

        bt_publish.setOnClickListener {
            var content = edt_content.text.toString().trim()
            if (content.isEmpty()) {
                ToastUtil.show(this, "通知内容不能为空")
                return@setOnClickListener
            }
            val selectedNodes = TreeHelper.getSelectedNodes(root);
            if (selectedNodes.size == 0) {
                ToastUtil.show(this, "您还没有选择接收人")
                return@setOnClickListener
            }

            showDialog("正在发布通知...")

            var notify = Notify()
            notify.content = content
            notify.publish = user.objectId
            notify.save(object : SaveListener<String>() {
                override fun done(objectId: String?, e: BmobException?) {
                    if (e == null) {
                        MAX_COUNT = selectedNodes.size
                        count = 0
                        for (i in selectedNodes.indices) {
                            var userInfo = selectedNodes.get(i).getValue() as UserInfo

                            var notifyReceiver = NotifyReceiver()
                            notifyReceiver.nid = objectId
                            notifyReceiver.uid = userInfo.objectId
                            notifyReceiver.isRead = false
                            notifyReceiver.save(object : SaveListener<String>() {
                                override fun done(objectId: String?, e: BmobException?) {
                                    count++
                                    if (count == MAX_COUNT) {
                                        showDialog("通知发布成功...")
                                        Handler().postDelayed({
                                            dismissDialogNow()
                                            finish()
                                        }, 1500)
                                    }
                                }
                            })
                        }
                    } else {
                        showDialog("通知发布失败...")
                        dismissDialog()
                    }
                }
            })
        }
    }

    override fun initData() {
        user = intent.getSerializableExtra("user") as UserInfo
        getData()
    }

    fun getData() {
        val categoryBmobQuery = BmobQuery<UserInfo>()
        categoryBmobQuery.findObjects(
                object : FindListener<UserInfo>() {
                    override fun done(list: List<UserInfo>?, e: BmobException?) {
                        if (e == null) {
                            Log.e("监理人员结果-----》", list?.size.toString())

                            when (user.role) {
                                3 -> {   // 市级领导
                                    list?.forEach { quser ->
                                        if (quser.role == 2) { // 区县
                                            val treeNode = TreeNode(quser)
                                            treeNode.level = 0
                                            list?.forEach { vuser ->
                                                if (vuser.areaId == quser.areaId && vuser.role == 4) {// 乡镇
                                                    val treeNode1 = TreeNode(vuser)
                                                    treeNode1.level = 1
                                                    list?.forEach { juser ->
                                                        if (juser.areaId == quser.areaId && juser.district.equals(vuser.district) && juser.role == 1) { // 监理
                                                            val treeNode2 = TreeNode(juser)
                                                            treeNode2.level = 2
                                                            treeNode1.addChild(treeNode2)
                                                        }
                                                    }
                                                    treeNode.addChild(treeNode1)
                                                }
                                            }
                                            root.addChild(treeNode)
                                        }
                                    }
                                }
                                2 -> {     // 区县领导
                                    list?.forEach { vuser ->
                                        if (vuser.areaId == user.areaId && vuser.role == 4) {// 乡镇
                                            val treeNode1 = TreeNode(vuser)
                                            treeNode1.level = 0
                                            list?.forEach { juser ->
                                                if (juser.areaId == user.areaId && juser.district.equals(vuser.district) && juser.role == 1) { // 监理
                                                    val treeNode2 = TreeNode(juser)
                                                    treeNode2.level = 1
                                                    treeNode1.addChild(treeNode2)
                                                }
                                            }
                                            root.addChild(treeNode1)
                                        }
                                    }

                                }
                                4 -> {     // 乡镇领导
                                    list?.forEach { juser ->
                                        if (juser.areaId == user.areaId && juser.district.equals(user.district) && juser.role == 1) { // 监理
                                            val treeNode2 = TreeNode(juser)
                                            treeNode2.level = 0
                                            root.addChild(treeNode2)
                                        }
                                    }
                                }
                            }
                            initTreeView()
                        }
                    }
                }
        )
    }

    lateinit var view: View
    private fun initTreeView() {
//        buildTree(root)
        if (root.children.size == 0) {
            view = layoutInflater.inflate(R.layout.common_empty, null)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            view.findViewById<TextView>(R.id.tv_empty).text = "没有找到接收人"
        } else {
            treeView = TreeView(root, this, MyNodeViewFactory())
            view = treeView.view
        }

        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        container.addView(view)
    }

    /**
     * 构建树形结构
     */
    private fun buildTree(root: TreeNode) {


        var mTitles = resources.getStringArray(com.qdhc.ny.R.array.areas)
        mTitles.forEachIndexed { index, title ->
            val treeNode = TreeNode(title)
            treeNode.level = 0

            ProjectData.getInstance().villages.forEach { village ->
                if (village.areaId == index + 1) {
                    val treeNode1 = TreeNode(village.name)
                    treeNode1.level = 1
                    var list = ProjectData.getInstance().villageUserMap.get(village.objectId)
                    if (list != null && list.size > 0) {
                        list.forEach { user ->
                            val treeNode2 = TreeNode(user.nickName)
                            treeNode2.level = 2
                            treeNode1.addChild(treeNode2)
                        }
                    }
                    treeNode.addChild(treeNode1)
                }
            }
            root.addChild(treeNode)
        }
    }
}
