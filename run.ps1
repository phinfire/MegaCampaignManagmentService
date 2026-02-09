$imageName = "megacampaignservice"
$env:JAVA_HOME = "C:\Users\phinf\.jdk\jdk-21.0.8"
docker stop $imageName 2>$null
docker rm $imageName 2>$null
.\mvnw.cmd clean package -P dev
docker build -t "${imageName}:latest" .
docker run --name $imageName -p 8080:8080 "${imageName}:latest"