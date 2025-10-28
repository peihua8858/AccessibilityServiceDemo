package com.peihua.touchmonitor.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import com.peihua.compose.file.mimeTypeFromFilePath
import com.peihua.compose.utils.LogCat
import com.peihua.touchmonitor.ServiceApplication
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Locale


private const val TAG = "BluetoothUtils"
private var BROADCAST_SERVICE_PARCEUUID = ParcelUuid.fromString("0000fff7-0000-1000-8000-000777f9b34fb")
private var MANUFACTURER_ID = 0x01

/**
 * 不得超过180000毫秒。值为0将禁用时间限制
 */
private val BROADCAST_SETTING_TIME = 10 * 1000
private val mBluetoothAdapter by lazy {
    val context = ServiceApplication.application
    val manager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    manager?.adapter
};
private var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

private var mBluetoothLeScanner: BluetoothLeScanner? = null
private const val g5Hz = "5"
private const val g24Hz = "2"
/**
 * 初始化广播设置
 *
 * @return AdvertiseSettings
 */
fun initBroadcastSetting(): AdvertiseSettings? {
    return AdvertiseSettings.Builder() //设置广播模式，以控制广播的功率和延迟。
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) //发射功率级别
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) //不得超过180000毫秒。值为0将禁用时间限制。
        .setTimeout(BROADCAST_SETTING_TIME) //设置是否可以连接
        .setConnectable(false)
        .build()
}

/**
 * 初始化广播包
 *
 * @return AdvertiseData
 */
fun initBroadcastPacketData(): AdvertiseData? {
    return AdvertiseData.Builder() //设置广播设备名称
        .setIncludeDeviceName(true) //设置发射功率级别
        .setIncludeDeviceName(true)
        .build()
}

/**
 * 初始化扫描响应包
 *
 * @param manufacturerData 厂商数据 byte[]
 * @return AdvertiseData
 */
fun initBroadcastScanData(manufacturerData: ByteArray?): AdvertiseData? {
    return AdvertiseData.Builder() //隐藏广播设备名称
        .setIncludeDeviceName(false) //隐藏发射功率级别
        .setIncludeDeviceName(false) //设置广播的服务UUID
        .addServiceUuid(BROADCAST_SERVICE_PARCEUUID) // .addServiceData()   也可用于添加数据
        //设置厂商数据
        .addManufacturerData(MANUFACTURER_ID, manufacturerData)
        .build()
}

/**
 * 发送广播
 *
 * @param this 广播数据
 */
@SuppressLint("MissingPermission")
fun ByteArray?.sendBroadcast(callback: AdvertiseCallback.() -> Unit) {
    this ?: return
    val multipleAdvertisementSupported = mBluetoothAdapter?.isMultipleAdvertisementSupported() ?: false
    if (multipleAdvertisementSupported) {
        val advertiseCallback = AdvertiseCallback().apply(callback)
        //获取BLE广播的操作对象。
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = mBluetoothAdapter?.getBluetoothLeAdvertiser()
        }
        //初始化广播数据
        val broadcastScanData = initBroadcastScanData(this)
        mBluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        //开启广播
        mBluetoothLeAdvertiser?.startAdvertising(
            initBroadcastSetting(),
            initBroadcastPacketData(), broadcastScanData, advertiseCallback
        )
        LogCat.d(TAG, "begin send ble broadcast.")
    } else {
        LogCat.d(TAG, "the phone chip does not support broadcasting")
    }
}

/**
 * 广播发送后的回调
 */
class AdvertiseCallback : android.bluetooth.le.AdvertiseCallback() {
    private var onStartFailure: ((Int) -> Unit?)? = null

    private var onStartSuccess: ((AdvertiseSettings?) -> Unit?)? = null
    infix fun onStartSuccess(callback: (AdvertiseSettings?) -> Unit): AdvertiseCallback {
        onStartSuccess = callback
        return this
    }

    infix fun onStartFailure(callback: (Int) -> Unit): AdvertiseCallback {
        onStartFailure = callback
        return this
    }

    override infix fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        LogCat.d(TAG, "broadcast send successfully$settingsInEffect")
        super.onStartSuccess(settingsInEffect)
        onStartSuccess?.invoke(settingsInEffect)
    }

    override infix fun onStartFailure(errorCode: Int) {
        LogCat.d(TAG, "broadcast sending failed：may be sending$errorCode")
        super.onStartFailure(errorCode)
        onStartFailure?.invoke(errorCode)
    }
}


/**
 * 通过bt发送文件
 *
 * @param [this] 上下文
 */
fun Context.sendFileByBt(filePath: String) {
    try {
        val fileAPK = File(filePath)
        if (!fileAPK.exists()) {
            LogCat.d(TAG, "sendApkByBt: fileAPK not exists")
            return
        }
        val uri = fileAPK.fileProvider
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.setType(filePath.mimeTypeFromFilePath)
        intent.setPackage("com.android.bluetooth")
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        LogCat.d(TAG, "sendApkByBt: startActivity success  appDir$fileAPK\n uri:$uri")
    } catch (e: java.lang.Exception) {
        LogCat.d(TAG, "sendApkByBt: e $e")
        e.printStackTrace()
    }
}


@SuppressLint("MissingPermission")
fun receiveBroadcastData(callback:ScanCallback.() -> Unit) {
    LogCat.d(TAG, "begin receive broadcasts")
    if (isEnabled()) {
        val scanCallback = ScanCallback().apply(callback)
        mBluetoothLeScanner = mBluetoothAdapter?.getBluetoothLeScanner()
        mBluetoothLeScanner?.startScan(createScanFilter(), ScanSettings.Builder().build(), scanCallback)
    } else {
        openBluetooth()
    }
}

@SuppressLint("MissingPermission")
fun unReceiveBroadcastData() {
    LogCat.d(TAG, "stop receiving broadcasts")
    try {
        if (mBluetoothLeScanner != null && isEnabled() && mBluetoothAdapter!!.getState() == BluetoothAdapter.STATE_ON) {
            mBluetoothLeScanner?.stopScan(mScanCallBack)
        }
    } catch (e: Exception) {
        LogCat.d(TAG, "unReceiveBroadcastData: Exception $e")
        e.printStackTrace()
    }
}

private fun createScanFilter(): MutableList<ScanFilter> {
    val scanFilters: MutableList<ScanFilter> = ArrayList()
    scanFilters.add(ScanFilter.Builder().setServiceUuid(BROADCAST_SERVICE_PARCEUUID).build())
    return scanFilters
}
class ScanCallback : android.bluetooth.le.ScanCallback() {
    private var onScanResult: ((Int, ScanResult?) -> Unit)? = null
    infix fun onScanResult(callback: (Int, ScanResult?) -> Unit): ScanCallback {
        onScanResult = callback
        return this
    }
    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        LogCat.d(TAG, "scan filed$errorCode")
        onScanResult?.invoke(errorCode, null)
    }
    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        onScanResult?.invoke(callbackType, result)
    }
}
/**
 * 扫描回调
 */
private val mScanCallBack:  android.bluetooth.le.ScanCallback = object :  android.bluetooth.le.ScanCallback() {
    @SuppressLint("MissingPermission")
    public override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        val paresResult: String? = onReceiveData(result)
//        is5GSupported(paresResult)
        LogCat.d(TAG, "scan was successful$callbackType")
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        LogCat.d(TAG, "scan filed$errorCode")
    }
}

///**
// * is5 gsupported
// *
// * @param parseResults 解析结果
// */
//private fun is5GSupported(parseResults: String?) {
//    if (g5Hz == parseResults || g24Hz == parseResults) {
//        if (g5Hz.equals(parseResults)) {
//            isSupport5GWifi = true
//        } else {
//            isSupport5GWifi = false
//        }
//        unReceiveBroadcastData()
//    }
//}
/**
 * ScanResult扫描的所有结果处理
 *
 * @param result
 */
@SuppressLint("MissingPermission")
fun onReceiveData(result: ScanResult?): String? {
    if (result == null) {
        LogCat.d(TAG, "result is null")
        return null
    }
    val record: ScanRecord? = result.scanRecord
    if (null == record) {
        LogCat.d(TAG, "result.getScanRecord is null")
        return null
    }
    var parseResults: String? = null
    val manufacturerSpecificData = record.getManufacturerSpecificData(MANUFACTURER_ID)
    parseResults = byteArr2Str(manufacturerSpecificData)
    LogCat.d(TAG, "parseResults: $parseResults")
    return parseResults
}

fun stringToByteArr(str: String): ByteArray {
    return str.toByteArray()
}

/**
 * byte[] 解析为String
 *
 * @param byteArr
 * @return
 */
fun byteArr2Str(byteArr: ByteArray?): String? {
    if (null == byteArr || byteArr.isEmpty()) return ""
    val sb = StringBuilder()
    for (t in byteArr) {
        if ((t.toInt() and 0xF0) == 0) sb.append("0")
        sb.append(Integer.toHexString(t.toInt() and 0xFF))
    }
    var hexResult = sb.toString()

    if (hexResult == "") {
        return null
    }
    hexResult = hexResult.replace(" ", "")
    val baKeyword = ByteArray(hexResult.length / 2)
    for (i in baKeyword.indices) {
        try {
            baKeyword[i] = (0xff and hexResult.substring(i * 2, i * 2 + 2).toInt(16)).toByte()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    try {
        hexResult = String(baKeyword, charset("UTF-8"))
    } catch (e1: java.lang.Exception) {
        LogCat.d(TAG, "hexResult error: $e1")
        e1.printStackTrace()
    }
    return hexResult
}

@SuppressLint("MissingPermission")
fun getBonded() {
    LogCat.d(TAG, "getBonded: ")
    val bondedDevices = mBluetoothAdapter!!.getBondedDevices()
    //		bondedDevices.iterator().
    for (device in bondedDevices) {
        LogCat.d(TAG, "Name:" + device.getName() + "   Mac:" + device.getAddress())

        try {
            //使用反射调用获取设备连接状态方法
            val isConnectedMethod: Method = BluetoothDevice::class.java.getDeclaredMethod("isConnected", null)
            isConnectedMethod.isAccessible = true
            val isConnected = isConnectedMethod.invoke(device, null) as Boolean
            LogCat.d(TAG, "isConnected：$isConnected")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}

/**
 * 字节数组--->16进制字符串
 *
 * @param bytes  字节数组
 * @param length 字节数组长度
 * @return 16进制字符串 有空格
 */
fun bytes2HexString(bytes: ByteArray, length: Int): String {
    val result = StringBuffer()
    var hex: String?
    for (i in 0..<length) {
        hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
        if (hex.length == 1) {
            hex = '0'.toString() + hex
        }
        result.append(hex.uppercase(Locale.getDefault())).append(" ")
    }
    return result.toString()
}

/**
 * 16进制字符串--->字节数组
 *
 * @param src 16进制字符串
 * @return byte[]
 */
fun hexString2Bytes(src: String): ByteArray {
    val strLength = src.length / 2
    val ret = ByteArray(strLength)
    for (i in 0..<strLength) {
        ret[i] = src.substring(i * 2, i * 2 + 2).toInt(16).toByte()
    }
    return ret
}

/**
 * 是否支持蓝牙
 *
 * @return
 */
fun isSupportBluetooth(): Boolean {
    return mBluetoothAdapter != null
}

/**
 * 判断设备是否打开了蓝牙。
 *
 * @return
 */
fun isEnabled(): Boolean {
    return mBluetoothAdapter != null && mBluetoothAdapter!!.isEnabled()
}

/**
 * 用户无感知打开蓝牙
 */
@SuppressLint("MissingPermission")
fun openBluetooth(): Boolean {
    return mBluetoothAdapter != null && mBluetoothAdapter!!.enable()
}


/**
 * 关闭蓝牙  个别品牌手机会弹出请求关闭和打开弹框
 *
 * @return [Boolean]
 */
@SuppressLint("MissingPermission")
fun closeBluetooth(): Boolean {
    return mBluetoothAdapter != null && mBluetoothAdapter!!.disable()
}

fun charToByte(c: Char): ByteArray {
    val ret = ByteArray(2)
    ret[0] = ((c.code and 0xFF00) shr 8).toByte()
    ret[1] = (c.code and 0xFF).toByte()
    return ret
}
