package com.shu.user.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import com.bigkoo.alertview.AlertView
import com.bigkoo.alertview.OnItemClickListener
import com.jph.takephoto.app.TakePhoto
import com.jph.takephoto.app.TakePhotoImpl
import com.jph.takephoto.compress.CompressConfig
import com.jph.takephoto.model.TResult
import com.shu.base.utils.AppPrefsUtils
import com.shu.base.utils.DateUtils
import com.shu.base.utils.GlideUtils
import com.shu.user.presenter.view.UserInfoView
import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCompletionHandler
import com.qiniu.android.storage.UploadManager
import com.shu.base.common.BaseConstant
import com.shu.base.ext.onClick
import com.shu.base.ui.activity.BaseMvpActivity
import com.shu.base.ui.activity.BaseTakePhotoActivity
import com.shu.provider.common.ProviderConstant
import com.shu.user.R
import com.shu.user.data.protocol.UserInfo
import com.shu.user.injection.component.DaggerUserComponent
import com.shu.user.injection.module.UserModule
import com.shu.user.presenter.LoginPresenter
import com.shu.user.presenter.UserInfoPresenter
import com.shu.user.presenter.view.LoginView
import com.shu.user.utils.UserPrefsUtils
import kotlinx.android.synthetic.main.activity_user_info.*
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File

/**
 * 用户注册界面
 */
class UserCenterActivity : BaseTakePhotoActivity<UserInfoPresenter>(), UserInfoView ,TakePhoto.TakeResultListener{

    private val mUploadManager: UploadManager by lazy { UploadManager() }

    private var mLocalFileUrl:String? = null
    private var mRemoteFileUrl:String? = null

    private var mUserIcon:String? = null
    private var mUserName:String? = null
    private var mUserMobile:String? = null
    private var mUserGender:String? = null
    private var mUserSign:String? = null




    override fun injectComponent() {
        DaggerUserComponent.builder().activityComponent(mActivityComponent).userModule(UserModule()).build().inject(this)
        mPresenter.mView = this
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        initView()
        initData()
    }

    /*
    初始化视图
 */
    private fun initView() {
        mUserIconView.onClick {
            showAlertView()
        }

        mHeaderBar.getRightView().onClick {
            mPresenter.editUser(mRemoteFileUrl!!,
                    mUserNameEt.text?.toString()?:"",
                    if(mGenderMaleRb.isChecked) "0" else "1",
                    mUserSignEt.text?.toString()?:""
            )
        }
    }

    /*
        初始化数据
     */
    private fun initData() {
        mUserIcon = AppPrefsUtils.getString(ProviderConstant.KEY_SP_USER_ICON)
        mUserName = AppPrefsUtils.getString(ProviderConstant.KEY_SP_USER_NAME)
        mUserMobile = AppPrefsUtils.getString(ProviderConstant.KEY_SP_USER_MOBILE)
        mUserGender = AppPrefsUtils.getString(ProviderConstant.KEY_SP_USER_GENDER)
        mUserSign = AppPrefsUtils.getString(ProviderConstant.KEY_SP_USER_SIGN)

        mRemoteFileUrl = mUserIcon
        if (mUserIcon != ""){
            GlideUtils.loadUrlImage(this,mUserIcon!!,mUserIconIv)
        }
        mUserNameEt.setText(mUserName)
        mUserMobileTv.text = mUserMobile

        if (mUserGender == "0") {
            mGenderMaleRb.isChecked = true
        }
        else {
            mGenderFemaleRb.isChecked = true
        }

        mUserSignEt.setText(mUserSign)

    }


    /*
           获取图片成功回调
        */
    override fun takeSuccess(result: TResult?) {

        mLocalFileUrl = result?.image?.compressPath
        mPresenter.getUploadToken()
    }


    /*
        获取上传凭证回调
     */
    override fun onGetUploadTokenResult(result: String) {
        mUploadManager.put(mLocalFileUrl,null,result,object: UpCompletionHandler {
            override fun complete(key: String?, info: ResponseInfo?, response: JSONObject?) {
                mRemoteFileUrl = BaseConstant.IMAGE_SERVER_ADDRESS + response?.get("hash")

                Log.d("test", mRemoteFileUrl)
                GlideUtils.loadUrlImage(this@UserCenterActivity, mRemoteFileUrl!!,mUserIconIv)
            }

        },null)
    }

    /*
        编辑用户资料回调
     */
    override fun onEditUserResult(result: UserInfo) {
        toast("修改成功")
        UserPrefsUtils.putUserInfo(result)
    }



}