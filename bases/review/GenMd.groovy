/**
 * 评审文档生成工具
 */
String filename = args[0]
File file = new File(filename)
XmlSlurper slurper = new XmlSlurper()

def review = slurper.parse(file)
new File(filename.replace('.xml', '.md')).withWriter { writer ->
    makeMd(review, writer)
}

def makeMd(review, Writer writer) {
    review.issues.issue.each { issue ->
        writer.println "# ${issue.@summary}"
        writer.println "**级别** ${issue.@priority} ${issue.@tags}"
        def history = issue.history
        writer.println "**评审人** ${history.@createdBy} ${history.@createdOn}"
        String desc = issue.desc.toString()
        if (desc) {
            writer.println "**描述**"
            writer.println '```'
            writer.println desc
            writer.println '```'
        }
        String filePath = issue.@filePath
        int lineStart = issue.@lineStart.toInteger() - 1
        int lineEnd = issue.@lineEnd.toInteger() + 2
        File sourceFile = new File('../' + filePath)
        writer.println "**源代码**\n```\n// file : ${issue.@filePath} $lineStart $lineEnd"
        if (sourceFile.exists() && sourceFile.canRead()) {
            def lines = new File('../' + filePath).readLines().subList(lineStart, lineEnd)
            lines.each {
                writer.println it
            }
        } else {
            writer.println '文件不可读，已经删除？'
        }
        writer.println '```'
    }
}
