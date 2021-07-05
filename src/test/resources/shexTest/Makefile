test: Manifests ShExTests

Manifests: schemas/manifest.jsonld validation/manifest.jsonld negativeSyntax/manifest.jsonld negativeStructure/manifest.jsonld

schemas/manifest.jsonld: schemas/manifest.ttl
	cd schemas && make manifest.jsonld

validation/manifest.jsonld: validation/manifest.ttl
	cd validation && make manifest.jsonld

negativeSyntax/manifest.jsonld: negativeSyntax/manifest.ttl
	cd negativeSyntax && make manifest.jsonld

negativeStructure/manifest.jsonld: negativeStructure/manifest.ttl
	cd negativeStructure && make manifest.jsonld

ShExTests: ShExJTests ShExVTests

ShExJTests: doc/ShExJ.jsg
	(ls schemas/*.json | grep -v coverage.json | xargs \
	 `npm bin`/json-grammar doc/ShExJ.jsg)

ShExVTests: doc/ShExV.jsg
	`npm bin`/json-grammar doc/ShExV.jsg validation/*.val
	`npm bin`/json-grammar doc/ShExV.jsg validation/*.err

