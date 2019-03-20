package marshi.android.mopubadmobadchoicesample

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.logging.MoPubLog
import com.mopub.nativeads.*
import marshi.android.mopubadmobadchoicesample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val UNIT_ID = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" //set your own mopub unit id
        const val ADMOB_ID = "ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx"
        const val TEST_DEVICE = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        MobileAds.initialize(this, ADMOB_ID) //set your own admob id.
        val googleBinder = MediaViewBinder.Builder(R.layout.activity_main)
            .mediaLayoutId(R.id.native_admob_media_layout)
            .iconImageId(R.id.native_icon_image)
            .titleId(R.id.native_title)
            .textId(R.id.native_text)
            .callToActionId(R.id.native_cta)
            // サンプルでは下記の通りprivacyInformationIconImageIdがセットされていましたが、ライブラリ内では破棄しているように見えます
            // https://developers.mopub.com/publishers/android/integrate-networks/#set-up-ad-renderers-for-native-ads
            // https://github.com/mopub/mopub-android-mediation/blob/master/AdMob/com/mopub/nativeads/GooglePlayServicesAdRenderer.java#L220-L221
//            .privacyInformationIconImageId(R.id.native_ad_daa_icon_image)
            // 前のバージョンまでpublicだったGooglePlayServicesAdRenderer.VIEW_BINDER_KEY_AD_CHOICES_ICON_CONTAINERがprivateに変更されています
            // ただし、一応今でもこの文字列でFrameLayoutを渡すと画像の描画はされませんが、タッチでイベントは発生するようです.
            // さらに、なぜかad_choices_containerをセットすると広告を押したときの遷移が発生しなくなります。
            .addExtra("ad_choices_container", R.id.admob_ad_choice)
            .build()
        val extras = mapOf("testDevices" to TEST_DEVICE) //set your own test device id.
        val sdkConfiguration = SdkConfiguration.Builder(UNIT_ID)
            .withMediationSettings(GooglePlayServicesNative.GooglePlayServicesMediationSettings(Bundle()))
            .withLogLevel(MoPubLog.LogLevel.DEBUG)
            .build()
        val googleAdRenderer = GooglePlayServicesAdRenderer(googleBinder)
        val listener: MoPubNative.MoPubNativeNetworkListener = object : MoPubNative.MoPubNativeNetworkListener {
            override fun onNativeLoad(nativeAd: NativeAd) {
                nativeAd.prepare(binding.nativeOuterView)
                nativeAd.renderAdView(binding.nativeOuterView)
            }

            override fun onNativeFail(errorCode: NativeErrorCode?) {
                Log.i("mopub", errorCode.toString())
            }
        }
        val mopubNative = MoPubNative(this, UNIT_ID, listener)
        mopubNative.registerAdRenderer(googleAdRenderer)
        mopubNative.setLocalExtras(extras)
        MoPub.canCollectPersonalInformation()
        MoPub.initializeSdk(this, sdkConfiguration) {
            mopubNative.makeRequest()
        }
    }
}
