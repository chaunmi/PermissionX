package com.permissionx.guolindev.compat

import android.content.Context

object PermissionCheckImpl: IPermissionCheck {

    private var headPermissionCheck: BasePermissionCheck? = null
    private var tailPermissionCheck: BasePermissionCheck? = null

    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        return headPermissionCheck?.isPermissionGranted(context, permission)?:false
    }

    init {
        addPermissionCheckToTail(ExternalStoragePermissionCheck())
        addPermissionCheckToTail(InstallPackagesPermissionCheck())
        addPermissionCheckToTail(NotificationPermissionCheck())
        addPermissionCheckToTail(BodySensorsPermissionCheck())
        addPermissionCheckToTail(AlarmPermissionCheck())
        addPermissionCheckToTail(BackgroundLocationPermissionCheck())
        addPermissionCheckToTail(AccessMediaLocationPermissionCheck())
        addPermissionCheckToTail(PictureInPicturePermissionCheck())
        addPermissionCheckToTail(ReadPhoneNumbersPermissionCheck())
        addPermissionCheckToTail(WifiPermissionCheck())
        addPermissionCheckToTail(BluetoothPermissionCheck())
        addPermissionCheckToTail(CommonPermissionCheck())
    }

    private fun addPermissionCheckToTail(permissionCheck: BasePermissionCheck) {
        if(headPermissionCheck == null) {
            headPermissionCheck = permissionCheck
        }
        tailPermissionCheck?.next = permissionCheck
        tailPermissionCheck = permissionCheck
    }

    fun addPermissionCheck(permissionCheck: BasePermissionCheck) {
        if(headPermissionCheck == null) {
            headPermissionCheck = permissionCheck
        }else {
            permissionCheck.next = headPermissionCheck
            headPermissionCheck = permissionCheck
        }
    }
}