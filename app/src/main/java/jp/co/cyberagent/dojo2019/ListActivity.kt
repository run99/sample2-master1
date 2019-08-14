package jp.co.cyberagent.dojo2019

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_get.*
import kotlinx.android.synthetic.main.activity_list.*
import kotlin.concurrent.thread

@Suppress("NAME_SHADOWING")
class ListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ViewAdapter(makeList(), this, this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        val db = Room.databaseBuilder(applicationContext,AppDatabase::class.java, "database-name").build()

        val data: Uri? = intent?.data
        Log.d("DATA", data.toString())

        if(data != null){
            val uri = Uri.parse(data.toString())
            val iam = uri.getQueryParameter("iam")
            val tw = uri.getQueryParameter("tw")
            val gh = uri.getQueryParameter("gh")

            val profile = Profile()
            profile.name = iam.toString()
            profile.github = tw.toString()
            profile.twitter = gh.toString()

            Toast.makeText(this, "ADD USER" + iam.toString(), Toast.LENGTH_LONG).show()

            thread { db.profileDao().insert(profile) }
        }

        scanButton.setOnClickListener {
            IntentIntegrator(this).initiateScan()
        }
    }

    private fun makeList(): List<Profile> {

        //永続データベースを作成
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build()

        //dbからデータを取得
        val dao = db.profileDao()

        //データリスト
        val dataList = mutableListOf<Profile>()

        dao.getAll().observe(this, Observer<List<Profile>> { profiles ->
            if (profiles != null) {
                for (i in profiles) {
                    val data: Profile = Profile().also {
                        it.name = i.name
                        it.github = i.github
                        it.twitter = i.twitter
                    }
                    dataList.add(data)
                }
            } else {
                Toast.makeText(this, "データは入っていません", Toast.LENGTH_LONG).show()
            }
            //二回目以降のデータ更新を通知
           recyclerView.adapter?.notifyDataSetChanged()
        })

        return dataList
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()


                val stringUrl = result.contents.toString()
                //QRコードを読み取った内容をUriに変換しなおしている
                val convertedUri = Uri.parse(stringUrl)
                //githubのアカウントの取得
                val githubAccount = convertedUri.getQueryParameter("gh")
                //twitterのアカウントの取得
                val twitterAccount = convertedUri.getQueryParameter("tw")
                //userNameのアカウントの取得
                val userName = convertedUri.getQueryParameter("iam")


                //Urlに変換したものは、String型にし直すことで、読み取ってくれるようになる
                val stringGithub = githubAccount.toString()
                val stringTwitter = twitterAccount.toString()
                val stringName = userName.toString()
                //  val stUrl = stringUrl.toString()


                //getActivityへ値を移行させる
                val intent = Intent(this, GetActivity::class.java)
                intent.putExtra("github", stringGithub)
                intent.putExtra("twitter", stringTwitter)
                intent.putExtra("name", stringName)

                //intent.putExtra("Url", stUrl)

                startActivity(intent)
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun onGithubClick(tappedView: View, profile: Profile){

        val intent = Intent(this, GithubActivity::class.java)
        intent.putExtra("GitHub", profile.github)

        startActivity(intent)

    }

    fun onTwitterClick(tappedView: View, profile: Profile){

        val intent = Intent(this, TwitterActivity::class.java)
        intent.putExtra("Twitter", profile.twitter)

        startActivity(intent)

    }

   /* fun onNameClick(tappdeView : View, profile: Profile){
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build()

        val profile = Profile()

        thread {
            db.profileDao().delete(profile)
        }

    }*/


   /* fun onItemClick(tappedView: View, profile: Profile) {
        val strList = arrayOf("githubアカウント：" + profile.github, "twitterアカウント：" + profile.twitter)

        // dialogの表示
        AlertDialog.Builder(tappedView.context) // FragmentではActivityを取得して生成
            .setTitle("リスト選択ダイアログ")
            .setItems(strList) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(this, "0", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        Toast.makeText(this, "1", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "else", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setPositiveButton("キャンセル") { _, _ -> }.show()
    }*/
}
