import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

fun OutputStream.appendLine(str: String) {
    this.write((str+"\n").toByteArray())
}

const val IndexPackageName = "com.albertvaka.classindexksp"
const val IndexFileName = "Index"
const val ConfigAnnotationName = "com.albertvaka.classindexksp.annotations"

class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val annotations: List<String>,
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val classesByIndex = mutableMapOf<String, MutableList<KSClassDeclaration>>()
    private val files = mutableSetOf<KSFile>()

    override fun finish() {
        val indexFile = codeGenerator.createNewFile(Dependencies(true, *files.toTypedArray()), IndexPackageName, IndexFileName)
        indexFile.appendLine("package $IndexPackageName\n")

        for ((indexName, symbols) in classesByIndex) {
            indexFile.appendLine("val $indexName = setOf(")
            for (symbol in symbols) {
                val asString = symbol.qualifiedName?.asString()
                indexFile.appendLine("\t$asString::class,")
            }
            indexFile.appendLine(")\n")
        }

        indexFile.close()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val deferred = mutableListOf<KSAnnotated>()
        for (annotation in annotations) {
            val allSymbols = resolver.getSymbolsWithAnnotation(annotation)
            val indexName = annotation.substringAfterLast('.')
            val validSymbols = mutableListOf<KSClassDeclaration>()

            for (symbol in allSymbols) {
                if (!symbol.validate()) {
                    logger.warn("Invalid symbol in ${symbol.containingFile}")
                    deferred.add(symbol)
                } else if (symbol !is KSClassDeclaration) {
                    logger.error("Non-class symbol annotated with '$annotation' in file '${symbol.containingFile}'")
                } else {
                    validSymbols.add(symbol)
                    symbol.containingFile?.let { files.add(it) }
                }
            }

            classesByIndex[indexName] = validSymbols
        }
        return deferred
    }

}

class BuilderProcessorProvider : SymbolProcessorProvider {

    private fun parseArgList(argName: String, environment : SymbolProcessorEnvironment) : List<String> {
        return environment.options.getOrDefault(argName, "")
            .splitToSequence(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
    }

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val annotations = parseArgList(ConfigAnnotationName, environment)
        if (annotations.isEmpty()) {
            throw IllegalStateException(
                "You need to indicate the annotations to process using the ksp argument '$ConfigAnnotationName'. Eg:\n" +
                        "ksp {\n    arg(\"$ConfigAnnotationName\", \"com.example.MyAnnotation, com.example.MyOtherAnnotation\")\n}"
            )
        }
        return BuilderProcessor(environment.codeGenerator, annotations, environment.logger)
    }

}
