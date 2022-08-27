set -e
cd open-work
npm install
npm run build
cd ..

cd open-web
npm install
npm run build
cd ..

cd open-server
mvn clean package
cd ..
ls open-server/target/open-platform-server*.jar
