import random
from datetime import datetime, timedelta
from elasticsearch import Elasticsearch
from tqdm import tqdm

# Elasticsearch 연결 설정
es = Elasticsearch(['http://localhost:9200'])

# 데이터 구성 요소 설정 - 1부터 시작하는 ROW_NUMBER()를 고려하여 빈 문자열을 첫 번째 요소로 추가
prefixes = [
    # 속성
    '불', '번개', '얼음', '바람', '대지',
    '신성', '암흑', '혼돈', '고대', '용족',
    '악마', '천상', '죽음', '생명', '정령',
    # 품질
    '영웅', '전설', '신화', '유물', '축복',
    '저주', '각성', '몰락', '태고', '심연',
    # 상태
    '강화', '정제', '순수', '타락', '불멸'
]

suffixes = [
    '의 날개', '의 영혼', '의 증표', '의 가호', '의 저주',
    '의 파편', '의 정수', '의 비호', '의 분노', '의 축복',
    '의 숨결', '의 눈물', '의 피', '의 심장', '의 발톱'
]

weapon_types = [
    '검', '대검', '단검', '창', '도끼',
    '활', '석궁', '지팡이', '망치', '단봉',
    '양손검', '할버드', '장창', '비수', '도',
    '채찍', '낫', '투척용 단검', '마법봉', '장책'
]

armor_types = [
    '천갑옷', '가죽갑옷', '판금갑옷', '로브', '망토',
    '투구', '장갑', '장화', '방패', '허리띠',
    '어깨보호구', '팔보호구', '다리보호구', '부적', '목걸이',
    '반지', '귀걸이', '팔찌', '벨트', '견갑'
]

consumable_types = [
    '체력 물약', '마나 물약', '저항 물약', '힘 물약', '민첩 물약',
    '지능 물약', '독 물약', '회복 물약', '부활 물약', '속도 물약',
    '순간이동 스크롤', '부활 스크롤', '귀환 스크롤', '확인 스크롤', '강화 스크롤'
]

accessory_types = [
    '룬', '보석', '문장', '깃털', '인장',
    '부적', '카드', '징표', '문양', '정수',
    '토템', '영혼석', '마력석', '성수', '부패석'
]

material_types = [
    '광석', '원목', '가죽', '천', '보석원석',
    '약초', '마력결정', '룬조각', '영혼파편', '용린',
    '몬스터의 핵', '정령의 눈물', '드래곤의 비늘', '마력의 심장', '신비한 가루'
]

category_map = {0: "무기", 1: "방어구", 2: "소비아이템", 3: "장신구", 4: "재료"}

def create_index_with_optimized_settings():
    if not es.indices.exists(index="items"):
        es.indices.create(index="items", body={
            "settings": {
                "refresh_interval": "-1",
                "number_of_replicas": 0,
                "analysis": {
                    "analyzer": {
                        "nori": {
                            "tokenizer": "nori_tokenizer"
                        }
                    }
                }
            },
            "mappings": {
                "properties": {
                    "name": {"type": "text", "analyzer": "nori"},
                    "description": {"type": "text", "analyzer": "nori"},
                    "price": {"type": "integer"},
                    "categoryName": {"type": "keyword"},
                    "imageUrl": {"type": "keyword"},
                    "downloadUrl": {"type": "keyword"},
                    "createdAt": {"type": "date"},
                    "updatedAt": {"type": "date"}
                }
            }
        })
        print("Index 'items' created with optimized settings.")

def get_item_name(n):
    item_type = n % 5

    prefix = prefixes[(n // 20) % len(prefixes)]

    if item_type == 0:  # 무기
        type_idx = (n // 700) % len(weapon_types)
        suffix_idx = (n // 14000) % len(suffixes)
        return f"{prefix} {weapon_types[type_idx]}{suffixes[suffix_idx]}"
    elif item_type == 1:  # 방어구
        type_idx = (n // 700) % len(armor_types)
        suffix_idx = (n // 14000) % len(suffixes)
        return f"{prefix} {armor_types[type_idx]}{suffixes[suffix_idx]}"
    elif item_type == 2:  # 소비아이템
        type_idx = (n // 700) % len(consumable_types)
        return f"{prefix} {consumable_types[type_idx]}"
    elif item_type == 3:  # 장신구
        type_idx = (n // 700) % len(accessory_types)
        return f"{prefix} {accessory_types[type_idx]}"
    else:  # 재료
        type_idx = (n // 700) % len(material_types)
        return f"{prefix} {material_types[type_idx]}"


def generate_bulk_data(total_docs):
    bulk_data = []

    for n in tqdm(range(1, total_docs + 1)):
        item_type = n % 5
        name = get_item_name(n)

        description_map = {
            0: '강력한 힘이 깃든 무기입니다. 적의 방어력을 무시하는 특수 효과가 있습니다.',
            1: '견고한 방어구입니다. 착용자의 생명력을 증가시키는 효과가 있습니다.',
            2: '신비한 효과가 있는 소비 아이템입니다. 사용 시 강력한 버프 효과를 얻습니다.',
            3: '고대의 힘이 깃든 장신구입니다. 착용자의 모든 능력치가 상승합니다.',
            4: '희귀한 제작 재료입니다. 최상급 아이템 제작에 사용됩니다.'
        }

        if item_type == 0:  # 무기
            price = int(random.random() * 990000 + 10000)
        elif item_type == 1:  # 방어구
            price = int(random.random() * 890000 + 110000)
        elif item_type == 2:  # 소비아이템
            price = int(random.random() * 49000 + 1000)
        elif item_type == 3:  # 장신구
            price = int(random.random() * 4900000 + 100000)
        else:  # 재료
            price = int(random.random() * 990000 + 10000)

        doc = {
            "index": {
                "_index": "items",
                "_id": str(n)
            }
        }

        data = {
            "name": name,
            "description": f"{description_map[item_type]} (No.{n})",
            "price": price,
            "categoryName": category_map[item_type],
            "imageUrl": f"/images/item_{n}.jpg",
            "downloadUrl": f"/downloads/item_{n}.zip",
            "createdAt": (datetime.now() - timedelta(days=random.randint(0, 365))).isoformat(),
            "updatedAt": (datetime.now() - timedelta(days=random.randint(0, 30))).isoformat()
        }

        bulk_data.append(doc)
        bulk_data.append(data)

        if len(bulk_data) >= 20000:
            es.options(request_timeout=60).bulk(body=bulk_data)
            bulk_data = []

    if bulk_data:
        es.options(request_timeout=60).bulk(body=bulk_data)

if __name__ == "__main__":
    create_index_with_optimized_settings()
    generate_bulk_data(10000000)

    # 데이터 삽입 후 인덱스 설정 복구
    es.indices.put_settings(index="items", body={
        "settings": {
            "refresh_interval": "1s",
            "number_of_replicas": 1
        }
    })