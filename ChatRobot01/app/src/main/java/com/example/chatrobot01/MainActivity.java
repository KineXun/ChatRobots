package com.example.chatrobot01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * 改bug，找方法，其中在布局文件那里卡了挺久的，因为组件的自适应让我头疼，其中测试了各种布局，有表格，有网格，有相对，有线性等等。
 * 因为开学，前前后后实际开发就那么几天，但是我还不满足于现在的版本
 * 我要实现左侧菜单的初始布局的探索，还要加上：打开应用时是否把之前的记录显示出来；聊天记录的删除选择；对话框如果一开始没点会“看不到”；笑话功能的完善；
 *                                         下面的对话布局背景与聊天背景接近，不好看；还有最后的语音播放（Android自带的不支持中文，淦！）
 *                                                                                                  -----2020.10.31,17:41.
 */

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == 200){
                String data = jsonTokener(msg.obj.toString());
                listUpdate(new MyDataSet("left",data));
            }
        }
    };

    private String textData;//输入框的文本数据
    private String textHead;//输入框的文本数据的头部
    private List<MyDataSet> dataList;//存储对话的集合
    private SQLiteConsole sqlConsole;//数据库控制台
    private MyAdapter adapter;//适配器
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appInit();

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {//监视单选条的选择
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setTextDataHead(checkedId);//根据单选项来改变发送语句的头部
            }
        });

    }

    //应用初始化
    public void appInit(){
        sqlConsole = new SQLiteConsole(this.getApplicationContext());
        dataList = sqlConsole.queryAll();//获取数据库中全部的数据
        adapter = new MyAdapter();
        listView = (ListView) findViewById(R.id.textlist);
        listView.setAdapter(adapter);//为listview组件添加适配器
        listView.setSelection(listView.getCount()-1);//设置当前选中的条目。假设当前屏幕只能显示5条信息，当有第6条数据添加进来时，
                                                    // 此方法会将第6条数据显示在屏幕上，将第1条数据滑出屏幕外
    }

    //根据单选条的选项来改变发送数据的头部
    public void setTextDataHead(int checkedId){

        /* 请求示例：http://api.qingyunke.com/api.php?key=free&appid=0&msg=你好
         *  key=free	(必需，固定值)
         *  appid=0	(可选，0表示智能识别)
         *  msg=你好	(必需，关键词，提交前请先经过 urlencode 处理，即：URLEncoder.encode("你好", "utf-8"))
         *  天气：msg=天气深圳                  中英翻译：msg=翻译i love you
         *  智能聊天：msg=你好                  笑话：msg=笑话
         *  歌词⑴：msg=歌词后来                 歌词⑵：msg=歌词后来-刘若英
         *  计算⑴：msg=计算1+1*2/3-4           计算⑵：msg=1+1*2/3-4
         *  ＩＰ⑴：msg=归属127.0.0.1          ＩＰ⑵：msg=127.0.0.1
         *  手机⑴：msg=归属13430108888        手机⑵：msg=13430108888
         *  成语查询：msg=成语一生一世          五笔/拼音：msg=好字的五笔/拼音
         * */

        switch (checkedId){
            case R.id.chat:
                //System.out.println("聊天");
                textHead = "";
                break;
            case R.id.translate:
                //System.out.println("翻译");
                textHead = "翻译";
                break;
            case R.id.joke:
                //System.out.println("天气");
                textHead = "天气";
                break;
            case R.id.lyric:
                //System.out.println("歌词");
                textHead = "歌词";
                break;
            case R.id.count:
                //System.out.println("计算");
                textHead = "计算";
                break;
            case R.id.ip:
                //System.out.println("IP");
                textHead = "归属";
                break;
            case R.id.phone:
                //System.out.println("手机");
                textHead = "归属";
                break;
            default:
                //System.out.println("成语查询");
                textHead = "成语";
                break;
        }
    }

    //获取青云客数据
    public void getData(View view){
        //关闭指定文本框的软键盘
        EditText editText = (EditText) findViewById(R.id.editText);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
        TextView textView = (TextView) findViewById(R.id.editText);
        textData = textView.getText().toString();//获取文本的数据
        if(textData.isEmpty()){
            return;
        }
        textView.setText("");
        listUpdate(new MyDataSet("right",textData));//把自己的对话放进去
        //忘了是哪个版本之后的网络请求都要放在线程里面了
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url;
                    Message msg = new Message();
                    //在模拟器中不需要http，但是在我自己的手机上需要更换成https协议，估计是模拟器的安卓版本还没到9
                    // （是9吗？我忘了是哪个版本开始就要求请求必须https协议来着）(￣.￣)
                    url = new URL("https://api.qingyunke.com/api.php?key=free&msg="+URLEncoder.encode(textHead+textData, "utf-8"));
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);// 连接超时5秒
                    conn.setRequestMethod("GET");//请求方式
                    if(conn.getResponseCode() == 200){//如果请求成功
                        msg.what = 200;
                        InputStream in = conn.getInputStream();//获取输入流
                        byte[] b = new byte[1024*512];//定义数组
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();//定义一个输出流
                        int len = 0;
                        while( ( len = in.read(b) ) > -1 ){
                            baos.write(b,0,len);
                        }
                        //转换成字符串
                        String data = baos.toString();
                        msg.obj = data;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //解析json数据,如:{"result":0,"content":"与其说是聊天，不如说是问答式的吧"}
    public String jsonTokener(String jsonData){
        String data = new String("");
        try {
            JSONObject jObject = new JSONObject(jsonData); // 头一个对象，可以取里面的result来判断有没有数据
            if(jObject.getString("result").equals("0")){ // 已经获取到青云客的回复数据
                data = jObject.getString("content");
                //把获取的数据中大的{br}变成换行符
                data = data.replace('{','#');
                data = data.replace('}','#');
                data = data.replaceAll("#br#","\n");
                data = data.replaceAll("#face:*#","");//简单运用正则替换字符串
                System.out.println("Data : "+data);//打印出获取的数据转换后的字符串，测试用
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    //listview的数据更新
    public void listUpdate(MyDataSet dataSet){
        sqlConsole.insert(dataSet);//把新的数据存进数据库
        dataList.add(dataSet);//把新的数据放进集合里
        adapter.notifyDataSetChanged();//刷新界面
        listView.setSelection(listView.getCount()-1);
    }

    //适配器类
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {//当调用notifyDataSetChanged方法时会自动执行此方法
            View view = convertView!=null?convertView:View.inflate(getApplicationContext(),R.layout.my_list_item,null);
            TextView textViewLeft = (TextView) view.findViewById(R.id.chatitemleft);
            TextView textViewRight = (TextView) view.findViewById(R.id.chatitemright);
            if(dataList.get(position).getPlace().equals("left")){
                textViewLeft.setText(dataList.get(position).getData());
                textViewLeft.setVisibility(View.VISIBLE);
                textViewRight.setVisibility(View.GONE);
            }else{
                textViewRight.setText(dataList.get(position).getData());
                textViewRight.setVisibility(View.VISIBLE);
                textViewLeft.setVisibility(View.GONE);
            }
            return view;
        }
    }

}
