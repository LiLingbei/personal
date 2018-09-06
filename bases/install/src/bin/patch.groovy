import com.google.common.base.Charsets
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

public class Patch {


    public static final String DIR = "dir"
    public static final String FILE = "file"
    public static final String DELETE_FIlES = "delete.json"
    public static final String UPDATE_FILES = "update.zip"
    public static final Map version = [
            "code"           : "{product}",
            "name"           : "自动化平台",
            "version"        : "v{version}",
            "previousVersion": "v{preversion}",
            "releaseDate"    : "2016-07-07",
            "pkType"         : "{pkType}",
            "os"             : "windows",
            "serviceName"    : "itone",
            "osBit"          : "64",
            "customCmd"      : "cd /d %root_path%&cd..&jdk\\bin\\java -Xms128m -Xmx256m -cp %root_path%\\lib\\3rd\\* groovy.ui.GroovyMain -c utf8 %root_path%\\bin\\patch.groovy -p %root_path% %root_path%\\updates\\tmp\\{product}-update-x64-win-{version}-{preversion}\\patch.zip",
            "description"    : [["info": "{info}"]]
    ]
    public static final Map script = [
            "webProjPath"   : "program",
            "sysFilePath"   : "",
            "dbUpdate"      : [
                    "isOper"     : "false",
                    "dbType"     : "mysql",
                    "cmdPathName": "pgsql/bin",
                    "user"       : "postgres",
                    "password"   : "",
                    "hostName"   : "127.0.0.1",
                    "dbName"     : "itone",
                    "params"     : "",
                    "schema"     : "itone"
            ],
            "configFileList": [],
            "addFileList"   : [],
            "delFileList"   : [],
            "updateFileList": []
    ]


    public static void main(String[] args) {
        if (args.length <= 0) {
            printUsage()
        }
        Patch patch = new Patch();
        switch (args[0]) {
            case "-c":
                args.length != 4 ? printUsage() : null
                patch.create(args[1], args[2], args[3])
                break;
            case "-p":
                args.length != 3 ? printUsage() : null
                patch.patch(args[1], args[2])
                break;
            case "-a":
                args.length != 3 ? printUsage() : null
                patch.add(args[1], args[2])
                break;
            default:
                printUsage()
        }
    }

    private static void printUsage() {
        println("usage1: -c oldPath newPath versionfile")
        println("usage2: -p path patchFile")
        println("usage3: -a path versionfile")
        System.exit(0)
    }

    public void add(String path, String versionFile) {
        def (product, String version, String preVersion) = getVersionInfo(versionFile)

        def delFile = new File(path, DELETE_FIlES)
        new File(DELETE_FIlES).write(delFile.exists() ? delFile.text : "{}", Charsets.UTF_8.name())

        zipRun(UPDATE_FILES) { zipIt ->
            new File(path).eachFileRecurse { file ->
                if (file.getName() != DELETE_FIlES) {
                    writeFileToZip(file, file.getCanonicalPath().replace(path, ""), zipIt)
                }
            }
        }
        genPatch(product, version, preVersion, versionFile)
    }

    private List getVersionInfo(String versionFile) {
        def matcher = versionFile =~ /(.*[\\/\\])?([^\\/\\\\]+)-v(\d*).(\d*).(\d*).txt$/
        if (!matcher.matches()) {
            println("versionFile must match productName-preVersion")
            println("   productName: product name on Version Server")
            println("   preVersion: like v1.0.1")
            System.exit(1)
        }
        println "file:%s, ${matcher[0]}"
        def (p1, p2, product, ver1, ver2, ver3) = matcher[0]
        String preVersion = "${ver1}.${ver2}.${ver3}"
        String version = "${ver1}.${ver2}.${ver3.toInteger() + 1}"
        [product, version, preVersion]
    }

    def zipRun(fileName, Closure col) {
        new File(fileName).withOutputStream { stream ->
            new ZipOutputStream(stream).withCloseable { it ->
                col.call(it)
            }
        }
    }

    private void newPatchJar(def product, String version, String preVersion, String info) {
        String patchName = "${product}-update-x64-win-${version}-${preVersion}"
        new File("./src", "installer/conf").mkdirs()
        new File("./src", "installer/bin").mkdirs()
        // 升级说明是特殊格式，必须使用v1.0.3(10-1)格式
        // String.replace使用时如果需要在被替换字符中使用\，必须使用\\\\替代
        String patchInfo = ("v${version}(${new Date().month}-1)\n" + info).readLines().join("\\\\n")
        ["version", "script"].each { it ->
            def text = JsonOutput.toJson(this[it]).
                    replaceAll('\\{product\\}', (String) product).
                    replaceAll('\\{version\\}', version).
                    replaceAll('\\{preversion\\}', preVersion).
                    replaceAll('\\{info\\}', patchInfo).
                    replaceAll('\\{pkType\\}', "升级包")
            new File("./src/installer/conf/${it}.json").write(text, Charsets.UTF_8.name())
        }
        zipRun("${patchName}.jar") { zipIt ->
            def files = ["patch.zip", "update.exe", "./src/installer/", "./src/installer/bin/", "./src/installer/conf/", "./src/installer/conf/script.json", "./src/installer/conf/version.json"]
            files.each { fileName ->
                def zipEntryName = "${patchName}/${fileName}".replace("./src/", "")
                writeFileToZip(new File(fileName), zipEntryName, zipIt)
            }
        }
    }

    public void create(String oldPath, String newPath, String versionFile) {
        def (product, String version, String preVersion) = getVersionInfo(versionFile)
        Map oldMap = this.getFileMaps(oldPath, new File(oldPath));
        Map newMap = this.getFileMaps(newPath, new File(newPath));
        Map delete = oldMap - newMap;
        Map add = newMap - oldMap;
        Map same = oldMap - delete;
        new File(DELETE_FIlES).write(JsonOutput.toJson(delete), Charsets.UTF_8.name());
        zipRun(UPDATE_FILES) { it ->
            for (Map.Entry entry : add.entrySet()) {
                writeFileToZip(newPath, entry.getKey(), it)
            }
            for (Map.Entry entry : same.entrySet()) {
                if (isDiff(entry.getKey(), oldPath, newPath)) {
                    writeFileToZip(newPath, entry.getKey(), it)
                }
            }
        }
        genPatch(product, version, preVersion, versionFile)
    }

    private void genPatch(product, String version, String preVersion, String versionFile) {
        newPatchZip()
        newPatchJar(product, version, preVersion, new File(versionFile).text)
        [UPDATE_FILES, DELETE_FIlES, "patch.zip", "script.json", "version.json"].
                each { it -> new File(it).delete() }
    }

    private void newPatchZip() {
        new FileOutputStream('patch.zip').withCloseable { stream ->
            new ZipOutputStream(stream).withCloseable { it ->
                writeFileToZip("./", DELETE_FIlES, it)
                writeFileToZip("./", UPDATE_FILES, it)
            }
        }
    }

    private boolean isDiff(String fileName, String source, String destination) {
        File sourceFile = new File(source, fileName);
        File destFile = new File(destination, fileName);
        if (destFile.isDirectory() || sourceFile.isDirectory()) {
            return true;
        };
        return md5(sourceFile) != md5(destFile);
    }

    private void writeFileToZip(File file, String name, def zipFile) {
        if (file.isDirectory()) {
            zipFile.putNextEntry(new ZipEntry(name.endsWith("/") ? name : name + "/"))
        } else {
            zipFile.putNextEntry(new ZipEntry(name))
            file.withInputStream { it -> zipFile << it }
        }
        zipFile.closeEntry()
    }

    private void writeFileToZip(String source, String fileName, def zipFile) {
        def file = new File(source + File.separator + fileName);
        def name = fileName
        if (file.isDirectory()) {
            name = name.replace(File.separator, '/') + '/'
        }
        writeFileToZip(file, name, zipFile)
    }

    public void patch(String path, String patchName) {
        //解压
        new ZipFile(patchName).withCloseable { zip ->
            for (ZipEntry entry : zip.entries()) {
                if (entry.getName() == DELETE_FIlES) {
                    new File(DELETE_FIlES).
                            withOutputStream { out -> out << zip.getInputStream(entry) }
                } else if (entry.getName() == UPDATE_FILES) {
                    new File(UPDATE_FILES).
                            withOutputStream { out -> out << zip.getInputStream(entry) }
                }
            }
        }
        //delele file
        Map deleteMap = new JsonSlurper().parse(new File(DELETE_FIlES), "utf-8");
        for (Map.Entry entry : deleteMap.entrySet()) {
            def file = new File(path, entry.getKey())
            if (file.exists()) {
                if (file.isDirectory() ? file.deleteDir() : file.delete()) {
//                    println("删除成功" + file.getCanonicalPath())
                } else {
                    println("删除失败" + file.getCanonicalPath())
                }
            }
        }
        // unzip file
        unzip(UPDATE_FILES, path, false)
    }

    public void unzip(String zipFilePath, String unzipFilePath, boolean includeZipFileName)
            throws Exception {
        File zipFile = new File(zipFilePath);
        //如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
        if (includeZipFileName) {
            String fileName = zipFile.getName();
            if (!fileName.isEmpty()) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            unzipFilePath = unzipFilePath + File.separator + fileName;
        }
        //创建解压缩文件保存的路径
        File unzipFileDir = new File(unzipFilePath);
        if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
            unzipFileDir.mkdirs();
        }

        //开始解压
        ZipFile zip = new ZipFile(zipFile);
        //循环对压缩包里的每一个文件进行解压
        for (ZipEntry entry : zip.entries()) {
            //构建解压后保存的文件夹路径
            def file = new File(unzipFilePath, entry.getName())
            if (entry.isDirectory()) {
                File dir = file;
                if (!dir.exists()) {
                    dir.mkdirs()
                };
                continue;
            }
            File parentDir = file.getParentFile();
            //如果文件夹路径不存在，则创建文件夹
            parentDir.mkdirs();
            //删除旧文件
            if (file.exists()) {
                file.delete();
            };
            //写入文件
            file.createNewFile();
            file << zip.getInputStream(entry);
        }
    }

    //显示目录的方法
    public Map<String, String> getFileMaps(final rootPath, File f) {
        String ignorePath = rootPath.endsWith(File.separator) ? rootPath :
                            rootPath + File.separator
        //判断传入对象是否为一个文件夹对象
        Map<String, String> map = [:]
        if (!f.isDirectory()) {
            System.out.println("你输入的不是一个文件夹，请检查路径是否有误！！");
            return map;
        } else {
            File[] t = f.listFiles();
            for (int i = 0; i < t.length; i++) {
                //判断文件列表中的对象是否为文件夹对象，如果是则执行tree递归，直到把此文件夹中所有文件输出为止
                String tmpPath = t[i].getCanonicalPath();
                tmpPath = tmpPath.replace(ignorePath, "");
                if (t[i].isDirectory()) {
                    map.put(tmpPath, DIR)
                    map.putAll(getFileMaps(ignorePath, t[i]));
                } else {
                    map.put(tmpPath, FILE)
                }
            }
            return map;
        }

    }
    /**
     * Generate md5 hash as a 32 char String for 'obj'
     * @param obj can be a File, InputStream or URL
     * @return
     */
    def md5(obj) {
        def hash = MessageDigest.getInstance('MD5').with {
            obj.eachByte(8192) { bfr, num ->
                update bfr, 0, num
            }
            it.digest()
        }
        new BigInteger(1, hash).toString(16).padLeft(32, '0')
    }
}