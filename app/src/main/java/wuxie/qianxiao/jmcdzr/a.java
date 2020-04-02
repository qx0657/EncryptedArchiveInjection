package wuxie.qianxiao.jmcdzr;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Create by Administrator
 * On 2020/4/1
 */
public class a {
    private Context context;
    private final String mydataname = "wuxiepojie";
    private String tempZipPath = "";
    private String datadirpath = "";
    private boolean isshowlog = true;
    private boolean isshowtoast = false;
    private final int Finish = 1;
    private final int INFO = 0;
    private final int ERROR = -1;
    private Handler handler;

    {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Finish:
                        if(isshowlog){
                            Log.i("jmcdzr", "存档注入完成");
                        }
                        if(isshowtoast){
                            t("存档注入完成");
                        }
                        break;
                    case INFO:
                        t((String) msg.obj);
                        break;
                    case ERROR:
                        Exception exception = (Exception) msg.obj;
                        if(isshowlog){
                            Log.e("jmcdzr", exception.toString());
                        }
                        String errorinfo = "错误："+exception.toString();

                        if(isshowtoast){
                            if(errorinfo.contains("Wrong Password")){
                                t("解压密码错误");
                            }else{
                                t(errorinfo);
                            }
                        }
                        File jhPath = new File(datadirpath + File.separator + mydataname);
                        boolean deleteresult = jhPath.delete();
                        if(isshowlog){
                            Log.i("jmcdzr","删除文件"+(deleteresult?"成功":"失败"));
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public a(Context context) {
        this.context = context;
        datadirpath = Objects.requireNonNull(context.getFilesDir().getParentFile()).getAbsolutePath();
        tempZipPath = datadirpath + File.separator + mydataname;
    }

    public void zr(final String pwd){
        zr(pwd,false);
    }

    public void zr(final String pwd, boolean isshowtoast){
        this.isshowtoast = isshowtoast;
        try {
            new Thread(() -> b(pwd)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void b(final String pwd) {
        try {
            c(mydataname,datadirpath);
            if(isshowlog){
                Log.i("jmcdzr","复制文件完成");
            }
            //复制完毕后解压文件
            ZipFile zipFile = new ZipFile(tempZipPath);
            if(!zipFile.isValidZipFile()){
                if(isshowlog){
                    Log.i("jmcdzr","非法zipFile");
                }
                if(isshowtoast){
                    Message msg = new Message();
                    msg.what = INFO;
                    msg.obj = "非法zipFile";
                    handler.sendMessage(msg);
                }
            }else{
                if(zipFile.isEncrypted()){
                    zipFile.setPassword(pwd.toCharArray());
                }
                zipFile.extractAll(datadirpath);
                if(isshowlog){
                    Log.i("jmcdzr","解压文件完成");
                }
                //解压完删除复制出的文件
                File jhPath = new File(tempZipPath);
                boolean deleteresult = jhPath.delete();
                if(isshowlog){
                    Log.i("jmcdzr","删除文件"+(deleteresult?"成功":"失败"));
                }
                //通知注入已完成
                Message msg = new Message();
                msg.what = Finish;
                handler.sendMessage(msg);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Message msg = new Message();
            msg.what = ERROR;
            msg.obj = e;
            handler.sendMessage(msg);
        }
    }

    private void c(String copyFile,String copytopath) throws IOException {
        File path = new File(copytopath);
        if(!path.exists()){
            path.mkdir();
        }
        File jhPath = new File(copytopath + File.separator + mydataname);
        //查看该文件是否存在
        if(jhPath.exists()){
            jhPath.delete();
        }
        //得到资源
        AssetManager am = context.getAssets();
        //得到该文件的输入流
        InputStream is = am.open(copyFile);
        //用输出流写到特定路径下
        FileOutputStream fos=new FileOutputStream(jhPath);
        //创建byte数组  用于1KB写一次
        byte[] buffer = new byte[1024];
        int count = 0;
        while((count = is.read(buffer))>0){
            fos.write(buffer,0,count);
        }
        //最后关闭流
        fos.flush();
        fos.close();
        is.close();
    }

    private void t(String s){
        Toast.makeText(context,s,Toast.LENGTH_SHORT).show();
    }
}
