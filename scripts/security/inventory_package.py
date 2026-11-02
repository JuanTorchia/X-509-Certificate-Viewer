"""Record the actual plugin ZIP JARs; missing packages are a failed inventory."""
import hashlib
import io
import json
import pathlib
import zipfile


def inventory_packages(directory):
    packages = sorted(directory.glob('*.zip'))
    if not packages:
        raise ValueError('No built plugin ZIP: packaged-library coverage is unavailable')
    result = []
    for package in packages:
        jars = []
        with zipfile.ZipFile(package) as plugin:
            for name in plugin.namelist():
                if not name.endswith('.jar'):
                    continue
                data = plugin.read(name)
                coordinates = []
                with zipfile.ZipFile(io.BytesIO(data)) as jar:
                    for metadata in jar.namelist():
                        if metadata.startswith('META-INF/maven/') and metadata.endswith('/pom.properties'):
                            properties = {}
                            for line in jar.read(metadata).decode('utf8').splitlines():
                                if '=' in line and not line.lstrip().startswith('#'):
                                    key, value = line.split('=', 1)
                                    properties[key.strip()] = value.strip()
                            if all(properties.get(key) for key in ('groupId', 'artifactId', 'version')):
                                coordinates.append(':'.join(properties[key] for key in ('groupId', 'artifactId', 'version')))
                jars.append({'path': name, 'sha256': hashlib.sha256(data).hexdigest(),
                             'embeddedMavenCoordinates': coordinates,
                             'classification': 'maven-metadata-present' if coordinates else 'project-or-unmapped-jar'})
        if not jars:
            raise ValueError(f'No JARs in {package.name}: investigate package shape')
        result.append({'package': package.name, 'sha256': hashlib.sha256(package.read_bytes()).hexdigest(), 'jars': jars})
    return {'schemaVersion': 1, 'packages': result, 'coverageLimits': [
        'A JAR without Maven metadata requires classification; it is not automatically clean',
        'Embedded metadata does not prove a complete inventory of shaded dependencies',
        'Non-JAR resources, bundled JavaScript and IDE-provided libraries are separate coverage']}


if __name__ == '__main__':
    directory = pathlib.Path('build/security')
    directory.mkdir(parents=True, exist_ok=True)
    result = inventory_packages(pathlib.Path('build/distributions'))
    (directory / 'package-inventory.json').write_text(json.dumps(result, indent=2) + '\n')
    print(f"Inventoried {len(result['packages'])} built plugin ZIP(s)")
