#!/bin/bash

aws dynamodb create-table \
    --table-name franchises \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 \
    --region us-east-1 \
    --no-cli-pager

echo "DynamoDB table 'franchises' created successfully"
