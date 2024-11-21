# Elasticsearch


### Index
```shell
PUT /items
{
  "settings": {
    "index": {
      "sort.field": "createdAt",
      "sort.order": "desc",
      "number_of_shards": 3,
      "number_of_replicas": 2,
      "analysis": {
        "analyzer": {
          "nori": {
            "type": "nori",
            "decompound_mode": "mixed",
            "stoptags": [
              "E",
              "IC",
              "J",
              "VV",
              "MAG", 
              "MAJ",
              "MM", 
              "SP", 
              "SSC", 
              "SSO", 
              "SC", 
              "SE",
              "XPN",
              "XSA", 
              "XSN", 
              "XSV",
              "UNA", 
              "NA",
              "VSV"
            ]
          }
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "name": {
        "type": "text",
        "analyzer": "nori"
      },
      "description": {
        "type": "text",
        "analyzer": "nori"
      },
      "price": {
        "type": "long"
      },
      "categoryName": {
        "type": "keyword"
      },
      "tags": {
        "type": "keyword"
      },
      "createdAt": {
        "type": "date",
        "format": "strict_date_optional_time||epoch_millis"
      }
    }
  }
}
```


### Index Template

```shell
PUT _index_template/access-log
{
  "index_patterns": ["access-log-*"],
  "template": {
    "aliases": {
      "access-log": { }
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        },
        "@version": {
          "type": "keyword"
        },
        "agent": {
          "properties": {
            "ephemeral_id": {
              "type": "keyword"
            },
            "hostname": {
              "type": "keyword"
            },
            "id": {
              "type": "keyword"
            },
            "name": {
              "type": "keyword"
            },
            "type": {
              "type": "keyword"
            },
            "version": {
              "type": "keyword"
            }
          }
        },
        "bytes": {
          "type": "keyword"
        },
        "client_ip": {
          "type": "keyword"
        },
        "duration": {
          "type": "keyword"
        },
        "ecs": {
          "properties": {
            "version": {
              "type": "keyword"
            }
          }
        },
        "host": {
          "properties": {
            "name": {
              "type": "keyword"
            }
          }
        },
        "http_method": {
          "type": "keyword"
        },
        "http_version": {
          "type": "keyword"
        },
        "input": {
          "properties": {
            "type": {
              "type": "keyword"
            }
          }
        },
        "log": {
          "properties": {
            "file": {
              "properties": {
                "path": {
                  "type": "keyword"
                }
              }
            },
            "offset": {
              "type": "long"
            }
          }
        },
        "message": {
          "type": "text"
        },
        "port": {
          "type": "keyword"
        },
        "referrer": {
          "type": "text"
        },
        "request": {
          "type": "text"
        },
        "response_code": {
          "type": "keyword"
        },
        "timestamp": {
          "type": "date",
          "format": "dd/MMM/yyyy:HH:mm:ss Z"
        },
        "type": {
          "type": "keyword"
        },
        "user_agent": {
          "type": "text"
        }
      }
    }
  },
  "priority": 500,
  "version": 1,
  "_meta": {
    "description": "Template for access logs",
    "created_by": "your_name"
  }
}
```

```shell
PUT _index_template/api-access-log
{
  "index_patterns": ["api-access-log-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 1
    },
    "aliases": {
      "api-access-log": { }
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        },
        "loggedAt": {
          "type": "date",
          "format": "dd/MMM/yyyy:HH:mm:ss Z"
        },
        "endpoint": {
          "type": "keyword"
        },
        "method": {
          "type": "keyword"
        },
        "statusCode": {
          "type": "integer"
        },
        "responseTime": {
          "type": "long"
        },
        "clientIp": {
          "type": "ip"
        },
        "userAgent": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "userId": {
          "type": "keyword"
        },
        "errorMessage": {
          "type": "text"
        },
        "additionalInfo": {
          "type": "object"
        },
        "geoip": {
          "properties": {
            "city_name": {
              "type": "keyword"
            },
            "country_name": {
              "type": "keyword"
            },
            "country_code2": {
              "type": "keyword"
            },
            "continent_code": {
              "type": "keyword"
            },
            "region_name": {
              "type": "keyword"
            },
            "location": {
              "type": "geo_point"
            }
          }
        }
      }
    }
  },
  "priority": 500,
  "version": 1,
  "_meta": {
    "description": "Template for API access logs",
    "created_by": "your_name"
  }
}

```