    1 package shop.whitedns.client.storm
    2
    3 import kotlinx.coroutines.Dispatchers
    4 import kotlinx.coroutines.withContext
    5 import java.net.InetAddress
    6 import kotlin.math.max
    7 import kotlin.math.min
    8
    9 /**
   10  * کلاسی برای تولید تنظیمات شبکه به صورت هوشمند و بر اساس محاسبات واقعی
   11  */
   12 data class GeneratedProfile(
   13     val minUp: Int, val maxUp: Int,
   14     val minDl: Int, val maxDl: Int,
   15     val resolverTimeout: Double,
   16     val dnsFragment: Int,
   17     val upDup: Int, val dlDup: Int,
   18     val compression: String
   19 )
   20
   21 object DynamicTuner {
   22
   23     /**
   24      * این متد با تست کردن ریزالور، بهترین مقادیر را برای شبکه فعلی محاسبه میکند
   25      */
   26     suspend fun generateOptimalProfile(resolverIp: String): GeneratedProfile = withContext(Dispatchers.IO) {
   27         val samples = 10
   28         var successfulPings = 0
   29         var totalRtt = 0L
   30
   31         // ۱. فاز شناسایی: تست پینگ و پکتلاست
   32         for (i in 1..samples) {
   33             val startTime = System.currentTimeMillis()
   34             try {
   35                 val address = InetAddress.getByName(resolverIp)
   36                 if (address.isReachable(800)) {
   37                     successfulPings++
   38                     totalRtt += (System.currentTimeMillis() - startTime)
   39                 }
   40             } catch (e: Exception) {}
   41         }
   42
   43         val lossRate = (samples - successfulPings).toDouble() / samples
   44         val avgRtt = if (successfulPings > 0) totalRtt / successfulPings else 1000L
   45
   46         // ۲. محاسبات هوشمند بر اساس دیتای به دست آمده
   47
   48         // تنظیم Fragment: در شبکههای ضعیف سایز کوچکتر برای پایداری بیشتر
   49         val calculatedFragment = if (lossRate > 0.3) 128 else if (avgRtt > 300) 256 else 512
   50
   51         // تنظیم تکرار پکت (Duplication): بر اساس نرخ گم شدن پکتها
   52         val calculatedUpDup = min(30, (lossRate * 50).toInt() + 2)
   53         val calculatedDlDup = min(60, (lossRate * 100).toInt() + 4)
   54
   55         // تنظیم زمان انتظار (Timeout): ۱.۵ برابر میانگین پینگ
   56         val calculatedTimeout = min(2.0, max(0.2, (avgRtt * 1.5) / 1000.0))
   57
   58         // تخمین سرعت بر اساس کیفیت شبکه
   59         val baseSpeed = if (avgRtt < 100) 2000 else 800
   60         val calculatedMaxDl = (baseSpeed / (lossRate + 1)).toInt()
   61
   62         // تصمیمگیری برای فشردهسازی
   63         val useCompression = if (avgRtt > 200 || lossRate > 0.2) "lz4" else "OFF"
   64
   65         GeneratedProfile(
   66             minUp = max(10, calculatedMaxDl / 10),
   67             maxUp = max(100, calculatedMaxDl / 4),
   68             minDl = max(50, calculatedMaxDl / 5),
   69             maxDl = calculatedMaxDl,
   70             resolverTimeout = calculatedTimeout,
   71             dnsFragment = calculatedFragment,
   72             upDup = calculatedUpDup,
   73             dlDup = calculatedDlDup,
   74             compression = useCompression
   75         )
   76     }
   77 }
