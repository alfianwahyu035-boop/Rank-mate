package org.gradle.wrapper;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** RankMate bootstrap wrapper, designed to be replaced by `gradle wrapper` after first sync. */
public final class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        File project = new File(System.getProperty("user.dir"));
        File propFile = new File(project,"gradle/wrapper/gradle-wrapper.properties");
        Properties properties=new Properties();
        try(FileInputStream in=new FileInputStream(propFile)){properties.load(in);}
        String url=properties.getProperty("distributionUrl", "https://services.gradle.org/distributions/gradle-8.9-bin.zip");
        String name=url.substring(url.lastIndexOf('/')+1);
        if(!name.matches("gradle-[0-9.]+-bin\\.zip")) throw new IOException("Unexpected distribution URL");
        String version=name.substring(7,name.length()-8); // gradle-8.9-bin.zip -> 8.9
        String gradleUserHome=System.getenv("GRADLE_USER_HOME");
        if(gradleUserHome==null||gradleUserHome.isEmpty())gradleUserHome=System.getProperty("user.home")+"/.gradle";
        File cache=new File(gradleUserHome,"rankmate-wrapper/"+version);
        File gradleBin=new File(cache,"gradle-"+version+"/bin/gradle"+(System.getProperty("os.name").toLowerCase().contains("win")?".bat":""));
        if(!gradleBin.isFile()){
            cache.mkdirs();
            File archive=new File(cache,name);
            if(!archive.isFile()){
                System.out.println("Downloading Gradle "+version+" for the first build...");
                URLConnection connection=new URL(url).openConnection();
                connection.setConnectTimeout(30000);connection.setReadTimeout(60000);
                File partial=new File(cache,name+".partial");
                try(InputStream in=connection.getInputStream(); OutputStream out=new FileOutputStream(partial)) {
                    byte[] buffer=new byte[128*1024];int len;
                    while((len=in.read(buffer))!=-1)out.write(buffer,0,len);
                }
                Files.move(partial.toPath(),archive.toPath(),StandardCopyOption.REPLACE_EXISTING);
            }
            String root=cache.getCanonicalPath()+File.separator;
            try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(new FileInputStream(archive)))){
                ZipEntry item;byte[] buffer=new byte[128*1024];
                while((item=zip.getNextEntry())!=null){
                    File file=new File(cache,item.getName());
                    String dest=file.getCanonicalPath();
                    if(!dest.startsWith(root))throw new IOException("Invalid zip entry "+item.getName());
                    if(item.isDirectory())file.mkdirs();
                    else{
                        file.getParentFile().mkdirs();
                        try(OutputStream out=new FileOutputStream(file)){
                            int n;while((n=zip.read(buffer))!=-1)out.write(buffer,0,n);
                        }
                    }
                }
            }
            if(!gradleBin.isFile())throw new IOException("Gradle distribution incomplete");
            if(!System.getProperty("os.name").toLowerCase().contains("win"))gradleBin.setExecutable(true);
        }
        List<String> command=new ArrayList<>();
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            command.add("cmd");command.add("/c");command.add(gradleBin.getAbsolutePath());
        }else command.add(gradleBin.getAbsolutePath());
        command.addAll(Arrays.asList(args));
        Process process=new ProcessBuilder(command).directory(project).inheritIO().start();
        int status=process.waitFor();
        if(status!=0)System.exit(status);
    }
}
