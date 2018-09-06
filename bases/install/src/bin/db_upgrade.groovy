import groovy.sql.Sql

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * ddl、custom、dml数据入库工具
 *
 * @author Haozhichao
 */
class DbUpgradeTool {

    public static final String DB_UPDATE_FILES = "db_update.zip"
    public static final String DB_UPDATE_DIR = "db_update"
    def schemas = ['platform','itone_data']

    def db_instance = Sql.newInstance("jdbc:postgresql://127.0.0.1:5432/itone", "postgres", "",
                                      "org.postgresql.Driver")

    def handleSqlFiles(String dbUpdatePath) {
        String ddlPath = dbUpdatePath + File.separator + DB_UPDATE_DIR + File.separator +
                         "program\\db\\ddl"
        String cusPath = dbUpdatePath + File.separator + DB_UPDATE_DIR + File.separator +
                         "program\\db\\custom"
        String dmlPath = dbUpdatePath + File.separator + DB_UPDATE_DIR + File.separator +
                         "program\\db\\dml"

        handleDdlSqlFiles(ddlPath)
        db_instance.execute("SET search_path = platform, itone_data, public;")
        handleCusSqlFiles(cusPath)
        handleDmlSqlFiles(dmlPath)
    }

    def handleCusSqlFiles(String cusPath) {
        File dir = new File(cusPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            String sqlContent = file.getText("utf-8")
            if (sqlContent != null && "" != sqlContent) {
                println "--- updating custom_sql, filename: ${file.name}"
                db_instance.execute(sqlContent)
            }
        }
    }

    def handleDdlSqlFiles(String ddlPath) {
        schemas.each { schema ->
            db_instance.execute("SET search_path =" +schema)
            String path = ddlPath + File.separator + schema
            if(new File(path).exists()) {
                createTrigger(path) // 创建触发器
                createTable(path) // 创建表
                createTableConstraint(path) // 创建约束
                createTableTrigger(path) // 创建触发器
            }
        }
    }

    def createTable(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('_table.sql')) {
                String sqlContent = file.getText("utf-8")
                if (sqlContent != null && "" != sqlContent) {
                    println "--- updating ddl_sql, filename: ${file.name}"
                    db_instance.execute(sqlContent)
                }
            }
        }
    }

    def createTrigger(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('.sql') && !file.name.endsWith('_trigger.sql') &&
                file.getParentFile().name == 'trigger') {
                println "--- updating trigger, filename: ${file.name}"
                db_instance.execute(file.getText("utf-8"))
            }
        }
    }

    def createTableConstraint(String ddlPath) {
        createPrimaryKey(ddlPath)
        createUniqueConstraint(ddlPath)
        createOtherConstraint(ddlPath)
    }

    def createTableTrigger(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('_trigger.sql')) {
                println "--- updating trigger, filename: ${file.name}"
                db_instance.execute(file.getText("utf-8"))
            }
        }
    }

    def createPrimaryKey(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('_constraint.sql')) {
                file.eachLine("utf-8") {
                    if (it.contains("PRIMARY KEY")) {
                        println "--- updating Constraint PrimaryKey, filename: ${file.name}"
                        db_instance.execute(it)
                    }
                }
            }
        }
    }

    def createUniqueConstraint(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('_constraint.sql')) {
                file.eachLine("utf-8") {
                    if (it.contains("UNIQUE")) {
                        println "--- updating Constraint UNIQUE, filename: ${file.name}"
                        db_instance.execute(it)
                    }
                }
            }
        }
    }

    def createOtherConstraint(String ddlPath) {
        File dir = new File(ddlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('_constraint.sql')) {
                file.eachLine("utf-8") {
                    if (!it.contains("PRIMARY KEY") && !it.contains("UNIQUE")) {
                        println "--- updating OtherConstraint, filename: ${file.name}"
                        db_instance.execute(it)
                    }
                }
            }
        }
    }

    def handleDmlSqlFiles(String dmlPath) {
        File dir = new File(dmlPath)
		if(!dir.exists()){
			return
		}
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('.sql')) {
                String sqlContent = file.getText("utf-8")
                if (sqlContent != null && "" != sqlContent) {
                    println "--- updating dml_sql, filename: ${file.name}"
                    db_instance.execute(sqlContent)
                }
            }
        }
    }

    def unDbUpdateFile(String dbUpdatePath) {
        def zipFilePath = dbUpdatePath + File.separator + DB_UPDATE_FILES
        def unzipFilePath = dbUpdatePath + File.separator + DB_UPDATE_DIR
        unzip(zipFilePath, unzipFilePath, false)
    }

    def unzip(String zipFilePath, String unzipFilePath, boolean includeZipFileName)
            throws Exception {
        println "zipFilePath: $zipFilePath"
        println "unzipFilePath: $unzipFilePath"

        File zipFile = new File(zipFilePath)
        // 如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
        if (includeZipFileName) {
            String fileName = zipFile.getName()
            if (!fileName.isEmpty()) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."))
            }
            unzipFilePath = unzipFilePath + File.separator + fileName
        }
        // 创建解压缩文件保存的路径
        File unzipFileDir = new File(unzipFilePath)
        if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
            unzipFileDir.mkdirs()
        }

        // 开始解压
        ZipFile zip = new ZipFile(zipFile)
        // 循环对压缩包里的每一个文件进行解压
        for (ZipEntry entry : zip.entries()) {
            //构建解压后保存的文件夹路径
            def file = new File(unzipFilePath, entry.getName())
            if (entry.isDirectory()) {
                File dir = file
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                continue
            }
            File parentDir = file.getParentFile()
            // 如果文件夹路径不存在，则创建文件夹
            parentDir.mkdirs()
            // 删除旧文件
            if (file.exists()) {
                file.delete()
            }
            // 写入文件
            file.createNewFile()
            file << zip.getInputStream(entry)
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            println("db upgrade,parameters input is incorrect!")
            System.exit(0)
        }

        new DbUpgradeTool().with {
            println "-------- start db upgrade --------"
            // 解压文件
            try {
                unDbUpdateFile(args[0])  // db_update.zip 所在目录
            } catch (Exception e) {
                println("--- unZip failed! e: " + e)
                System.exit(0)
            }
            // 文件中数据进行入库操作
            try {
                handleSqlFiles(args[0])
            } catch (Exception e) {
                println("--- handleSqlFiles failed! e: " + e)
            }
            println "-------- end db upgrade   --------"
        }
    }
}
