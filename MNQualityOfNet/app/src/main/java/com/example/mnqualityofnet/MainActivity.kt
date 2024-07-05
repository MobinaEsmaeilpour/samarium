package com.example.mnqualityofnet

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.CellInfoGsm
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import kotlin.system.measureNanoTime


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    //private lateinit var map: GoogleMap
    private var mGoogleMap: GoogleMap? = null
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var db: AppDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
//    @RequiresApi(Build.VERSION_CODES.R)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        db = AppDatabase.getDatabase(this)
//
//        if (!hasPermissions()) {
//            requestPermissions()
//        } else {
//            startTracking()
//        }
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ),
            1
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationChanged(it)
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    override fun onMapReady(googleMap: GoogleMap) {
//        map = googleMap
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            return
//        }
//        map.isMyLocationEnabled = true
//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            location?.let {
//                val currentLatLng = LatLng(it.latitude, it.longitude)
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//                map.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
//                val polylineOptions = PolylineOptions().add(currentLatLng)
//                map.addPolyline(polylineOptions)
//            }
//        }
//        collectSignalData()
//    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mGoogleMap?.let { onMapReady(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun collectSignalData() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            return
        }
        val cellInfoList = telephonyManager.allCellInfo
        for (cellInfo in cellInfoList) {
            when (cellInfo) {
                is CellInfoLte -> {
                    val cellIdentityLte = cellInfo.cellIdentity as CellIdentityLte
                    val cellSignalStrengthLte = cellInfo.cellSignalStrength as CellSignalStrengthLte
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            val measurementLTE = Measurement(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                timestamp = System.currentTimeMillis(),
                                technology = "LTE",
                                plmnId = cellIdentityLte.mccString + cellIdentityLte.mncString,
                                lac = 0,
                                tac = cellIdentityLte.tac,
                                cellId = cellIdentityLte.ci.toLong(),
                                rsrp = cellSignalStrengthLte.rsrp,
                                rsrq = cellSignalStrengthLte.rsrq,
                                rscp = null,
                                eCNo = null,
                                rac = null
                            )
                            lifecycleScope.launch {
                                db.measurementDao().insert(measurementLTE)
                            }
                        }
                    }
                }

                is CellInfoGsm -> {
                    val cellIdentityGsm = cellInfo.cellIdentity as CellIdentityGsm
                    val cellSignalStrengthGsm = cellInfo.cellSignalStrength as CellSignalStrengthGsm
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            val measurementGSM = Measurement(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                timestamp = System.currentTimeMillis(),
                                technology = "GSM",
                                plmnId = cellIdentityGsm.mccString + cellIdentityGsm.mncString,
                                lac = cellIdentityGsm.lac,
                                tac = null,
                                cellId = cellIdentityGsm.cid.toLong(),
                                rsrp = null,
                                rsrq = null,
                                rscp = null,
                                eCNo = null,
                                rac = null
                            )
                            lifecycleScope.launch {
                                db.measurementDao().insert(measurementGSM)
                            }
                        }
                    }
                }

                is CellInfoWcdma -> {
                    val cellIdentityWcdma = cellInfo.cellIdentity as CellIdentityWcdma
                    val cellSignalStrengthWcdma =
                        cellInfo.cellSignalStrength as CellSignalStrengthWcdma
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            val measurementwcdma = Measurement(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                timestamp = System.currentTimeMillis(),
                                technology = "UMTS",
                                plmnId = cellIdentityWcdma.mccString + cellIdentityWcdma.mncString,
                                lac = cellIdentityWcdma.lac,
                                tac = null,
                                cellId = cellIdentityWcdma.cid.toLong(),
                                rsrp = null,
                                rsrq = null,
                                rscp = cellSignalStrengthWcdma.dbm,
                                eCNo = cellSignalStrengthWcdma.ecNo,
                                rac = null
                            )
                            lifecycleScope.launch {
                                db.measurementDao().insert(measurementwcdma)
                            }
                        }
                    }
                }

                is CellInfoNr -> {
                    val cellIdentityNr = cellInfo.cellIdentity as CellIdentityNr
                    val cellSignalStrengthNr = cellInfo.cellSignalStrength as CellSignalStrengthNr
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        location?.let {
                            val measurementnr = Measurement(
                                latitude = it.latitude,
                                longitude = it.longitude,
                                timestamp = System.currentTimeMillis(),
                                technology = "5G",
                                plmnId = cellIdentityNr.mccString + cellIdentityNr.mncString,
                                lac = null,
                                tac = cellIdentityNr.tac,
                                cellId = cellIdentityNr.nci,
                                rsrp = cellSignalStrengthNr.ssRsrp,
                                rsrq = cellSignalStrengthNr.ssRsrq,
                                rscp = null,
                                eCNo = null,
                                rac = null
                            )
                            lifecycleScope.launch {
                                db.measurementDao().insert(measurementnr)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNetworkType(): String {
        return when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            else -> "UNKNOWN"
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun onLocationChanged(location: Location) {
        collectSignalData()
    }
}
