package uk.neilgall.rulesapp.parser

fun RuleSet<String>.resolve(): RuleSet<Attribute> {
    val attributesByName = attributes.associate({ it.name to it })
    val dynamicAttributes: MutableList<Attribute> = mutableListOf()
    fun lookup(name: String): Attribute {
        return attributesByName.getOrElse(name, {
            if (name.startsWith("\"") && name.endsWith("\"")) {
                // Promote quoted strings to constant attributes
                val a = Attribute.Constant("const${dynamicAttributes.size}", name.removeSurrounding("\""))
                dynamicAttributes.add(a)
                a
            } else {
                throw NoSuchElementException(name)
            }
        })
    }
    val resolvedRules = rules.map { it.map { lookup(it) }}

    return RuleSet(
            attributes + dynamicAttributes,
            resolvedRules
    )
}
