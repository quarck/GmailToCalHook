package com.github.quarck.gmail2calhk

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fDigg();
    }

    public override fun onStart()
    {
        super.onStart()
    }

    private fun fDigg()
    {
        val builder = AlertDialog.Builder(this)
        builder
                .setMessage("No access")
                .setCancelable(false)
                .setPositiveButton("F-Digg!") {
                    x, y ->
                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    startActivity(intent)
                }
                .setNegativeButton("Ignore!") {
                    DialogInterface, Int -> finish()
                }

        builder.create().show()
    }

    public override fun onStop()
    {
        super.onStop()
    }

    public override fun onResume() {
        super.onResume()

        if (!PermissionsManager.hasAllPermissions(this))
            PermissionsManager.requestPermissions(this)
    }
}
