-- 기존 카테고리 데이터 삭제 (필요한 경우)
TRUNCATE TABLE category RESTART IDENTITY CASCADE;
TRUNCATE TABLE item RESTART IDENTITY CASCADE;


-- 카테고리 추가
INSERT INTO category (name)
VALUES
    ('무기'),      -- id: 1
    ('방어구'),    -- id: 2
    ('소비아이템'), -- id: 3
    ('장신구'),    -- id: 4
    ('재료');      -- id: 5

-- 1000만건
WITH RECURSIVE numbers AS (
    SELECT 1 as n
    UNION ALL
    SELECT n + 1
    FROM numbers
    WHERE n < 10000000
),
               prefixes AS (
                   SELECT ROW_NUMBER() OVER () as id, prefix FROM (
                                                                      SELECT unnest(ARRAY[
                                                                          -- 속성
                                                                          '불', '번개', '얼음', '바람', '대지',
                                                                          '신성', '암흑', '혼돈', '고대', '용족',
                                                                          '악마', '천상', '죽음', '생명', '정령',
                                                                          -- 품질
                                                                          '영웅', '전설', '신화', '유물', '축복',
                                                                          '저주', '각성', '몰락', '태고', '심연',
                                                                          -- 상태
                                                                          '강화', '정제', '순수', '타락', '불멸'
                                                                          ]) as prefix
                                                                  ) p
               ),
               suffixes AS (
                   SELECT ROW_NUMBER() OVER () as id, suffix FROM (
                                                                      SELECT unnest(ARRAY[
                                                                          '의 날개', '의 영혼', '의 증표', '의 가호', '의 저주',
                                                                          '의 파편', '의 정수', '의 비호', '의 분노', '의 축복',
                                                                          '의 숨결', '의 눈물', '의 피', '의 심장', '의 발톱'
                                                                          ]) as suffix
                                                                  ) s
               ),
               weapon_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '검', '대검', '단검', '창', '도끼',
                                                                        '활', '석궁', '지팡이', '망치', '단봉',
                                                                        '양손검', '할버드', '장창', '비수', '도',
                                                                        '채찍', '낫', '투척용 단검', '마법봉', '장책'
                                                                        ]) as type
                                                                ) w
               ),
               armor_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '천갑옷', '가죽갑옷', '판금갑옷', '로브', '망토',
                                                                        '투구', '장갑', '장화', '방패', '허리띠',
                                                                        '어깨보호구', '팔보호구', '다리보호구', '부적', '목걸이',
                                                                        '반지', '귀걸이', '팔찌', '벨트', '견갑'
                                                                        ]) as type
                                                                ) a
               ),
               consumable_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '체력 물약', '마나 물약', '저항 물약', '힘 물약', '민첩 물약',
                                                                        '지능 물약', '독 물약', '회복 물약', '부활 물약', '속도 물약',
                                                                        '순간이동 스크롤', '부활 스크롤', '귀환 스크롤', '확인 스크롤', '강화 스크롤'
                                                                        ]) as type
                                                                ) p
               ),
               accessory_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '룬', '보석', '문장', '깃털', '인장',
                                                                        '부적', '카드', '징표', '문양', '정수',
                                                                        '토템', '영혼석', '마력석', '성수', '부패석'
                                                                        ]) as type
                                                                ) acc
               ),
               material_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '광석', '원목', '가죽', '천', '보석원석',
                                                                        '약초', '마력결정', '룬조각', '영혼파편', '용린',
                                                                        '몬스터의 핵', '정령의 눈물', '드래곤의 비늘', '마력의 심장', '신비한 가루'
                                                                        ]) as type
                                                                ) m
               )
INSERT INTO item (
    name,
    description,
    price,
    category_id,
    image_url,
    download_url,
    created_at,
    updated_at
)
SELECT
    CASE MOD(n, 5)
        WHEN 0 THEN  -- 무기
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM weapon_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 20)) ||
            (SELECT suffix FROM suffixes WHERE id = 1 + MOD(FLOOR(n/14000)::integer, 15))
        WHEN 1 THEN  -- 방어구
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM armor_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 20)) ||
            (SELECT suffix FROM suffixes WHERE id = 1 + MOD(FLOOR(n/14000)::integer, 15))
        WHEN 2 THEN  -- 소비아이템
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM consumable_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        WHEN 3 THEN  -- 장신구
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM accessory_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        ELSE        -- 재료
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM material_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        END as name,
    CASE MOD(n, 5)
        WHEN 0 THEN '강력한 힘이 깃든 무기입니다. 적의 방어력을 무시하는 특수 효과가 있습니다.'
        WHEN 1 THEN '견고한 방어구입니다. 착용자의 생명력을 증가시키는 효과가 있습니다.'
        WHEN 2 THEN '신비한 효과가 있는 소비 아이템입니다. 사용 시 강력한 버프 효과를 얻습니다.'
        WHEN 3 THEN '고대의 힘이 깃든 장신구입니다. 착용자의 모든 능력치가 상승합니다.'
        ELSE '희귀한 제작 재료입니다. 최상급 아이템 제작에 사용됩니다.'
        END || ' (No.' || n || ')' as description,
    CASE MOD(n, 5)
        WHEN 0 THEN (random() * 990000 + 10000)::integer  -- 무기: 1만~100만
        WHEN 1 THEN (random() * 890000 + 110000)::integer  -- 방어구: 11만~100만
        WHEN 2 THEN (random() * 49000 + 1000)::integer    -- 소비아이템: 1천~5만
        WHEN 3 THEN (random() * 4900000 + 100000)::integer -- 장신구: 10만~500만
        ELSE (random() * 990000 + 10000)::integer         -- 재료: 1만~100만
        END as price,
    CASE MOD(n, 5)
        WHEN 0 THEN 1  -- 무기
        WHEN 1 THEN 2  -- 방어구
        WHEN 2 THEN 3  -- 소비아이템
        WHEN 3 THEN 4  -- 장신구
        ELSE 5         -- 재료
        END as category_id,
    '/images/item_' || n || '.jpg' as image_url,
    '/downloads/item_' || n || '.zip' as download_url,
    now() - (random() * (interval '365 days')) as created_at,
    now() - (random() * (interval '30 days')) as updated_at
FROM numbers;


-- 100만건
WITH RECURSIVE numbers AS (
    SELECT 1 as n
    UNION ALL
    SELECT n + 1
    FROM numbers
    WHERE n < 1000000  -- 100만건으로 수정
),
               prefixes AS (
                   SELECT ROW_NUMBER() OVER () as id, prefix FROM (
                                                                      SELECT unnest(ARRAY[
                                                                          -- 속성
                                                                          '불', '번개', '얼음', '바람', '대지',
                                                                          '신성', '암흑', '혼돈', '고대', '용족',
                                                                          '악마', '천상', '죽음', '생명', '정령',
                                                                          -- 품질
                                                                          '영웅', '전설', '신화', '유물', '축복',
                                                                          '저주', '각성', '몰락', '태고', '심연',
                                                                          -- 상태
                                                                          '강화', '정제', '순수', '타락', '불멸'
                                                                          ]) as prefix
                                                                  ) p
               ),
               suffixes AS (
                   SELECT ROW_NUMBER() OVER () as id, suffix FROM (
                                                                      SELECT unnest(ARRAY[
                                                                          '의 날개', '의 영혼', '의 증표', '의 가호', '의 저주',
                                                                          '의 파편', '의 정수', '의 비호', '의 분노', '의 축복',
                                                                          '의 숨결', '의 눈물', '의 피', '의 심장', '의 발톱'
                                                                          ]) as suffix
                                                                  ) s
               ),
               weapon_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '검', '대검', '단검', '창', '도끼',
                                                                        '활', '석궁', '지팡이', '망치', '단봉',
                                                                        '양손검', '할버드', '장창', '비수', '도',
                                                                        '채찍', '낫', '투척용 단검', '마법봉', '장책'
                                                                        ]) as type
                                                                ) w
               ),
               armor_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '천갑옷', '가죽갑옷', '판금갑옷', '로브', '망토',
                                                                        '투구', '장갑', '장화', '방패', '허리띠',
                                                                        '어깨보호구', '팔보호구', '다리보호구', '부적', '목걸이',
                                                                        '반지', '귀걸이', '팔찌', '벨트', '견갑'
                                                                        ]) as type
                                                                ) a
               ),
               consumable_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '체력 물약', '마나 물약', '저항 물약', '힘 물약', '민첩 물약',
                                                                        '지능 물약', '독 물약', '회복 물약', '부활 물약', '속도 물약',
                                                                        '순간이동 스크롤', '부활 스크롤', '귀환 스크롤', '확인 스크롤', '강화 스크롤'
                                                                        ]) as type
                                                                ) p
               ),
               accessory_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '룬', '보석', '문장', '깃털', '인장',
                                                                        '부적', '카드', '징표', '문양', '정수',
                                                                        '토템', '영혼석', '마력석', '성수', '부패석'
                                                                        ]) as type
                                                                ) acc
               ),
               material_types AS (
                   SELECT ROW_NUMBER() OVER () as id, type FROM (
                                                                    SELECT unnest(ARRAY[
                                                                        '광석', '원목', '가죽', '천', '보석원석',
                                                                        '약초', '마력결정', '룬조각', '영혼파편', '용린',
                                                                        '몬스터의 핵', '정령의 눈물', '드래곤의 비늘', '마력의 심장', '신비한 가루'
                                                                        ]) as type
                                                                ) m
               )
INSERT INTO item (
    name,
    description,
    price,
    category_id,
    image_url,
    download_url,
    created_at,
    updated_at
)
SELECT
    CASE MOD(n, 5)
        WHEN 0 THEN  -- 무기
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM weapon_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 20)) ||
            (SELECT suffix FROM suffixes WHERE id = 1 + MOD(FLOOR(n/14000)::integer, 15))
        WHEN 1 THEN  -- 방어구
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM armor_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 20)) ||
            (SELECT suffix FROM suffixes WHERE id = 1 + MOD(FLOOR(n/14000)::integer, 15))
        WHEN 2 THEN  -- 소비아이템
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM consumable_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        WHEN 3 THEN  -- 장신구
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM accessory_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        ELSE        -- 재료
            (SELECT prefix FROM prefixes WHERE id = 1 + MOD(FLOOR(n/20)::integer, 35)) ||
            ' ' ||
            (SELECT type FROM material_types WHERE id = 1 + MOD(FLOOR(n/700)::integer, 15))
        END as name,
    CASE MOD(n, 5)
        WHEN 0 THEN '강력한 힘이 깃든 무기입니다. 적의 방어력을 무시하는 특수 효과가 있습니다.'
        WHEN 1 THEN '견고한 방어구입니다. 착용자의 생명력을 증가시키는 효과가 있습니다.'
        WHEN 2 THEN '신비한 효과가 있는 소비 아이템입니다. 사용 시 강력한 버프 효과를 얻습니다.'
        WHEN 3 THEN '고대의 힘이 깃든 장신구입니다. 착용자의 모든 능력치가 상승합니다.'
        ELSE '희귀한 제작 재료입니다. 최상급 아이템 제작에 사용됩니다.'
        END || ' (No.' || n || ')' as description,
    CASE MOD(n, 5)
        WHEN 0 THEN (random() * 990000 + 10000)::integer  -- 무기: 1만~100만
        WHEN 1 THEN (random() * 890000 + 110000)::integer  -- 방어구: 11만~100만
        WHEN 2 THEN (random() * 49000 + 1000)::integer    -- 소비아이템: 1천~5만
        WHEN 3 THEN (random() * 4900000 + 100000)::integer -- 장신구: 10만~500만
        ELSE (random() * 990000 + 10000)::integer         -- 재료: 1만~100만
        END as price,
    CASE MOD(n, 5)
        WHEN 0 THEN 1  -- 무기
        WHEN 1 THEN 2  -- 방어구
        WHEN 2 THEN 3  -- 소비아이템
        WHEN 3 THEN 4  -- 장신구
        ELSE 5         -- 재료
        END as category_id,
    '/images/item_' || n || '.jpg' as image_url,
    '/downloads/item_' || n || '.zip' as download_url,
    now() - (random() * (interval '365 days')) as created_at,
    now() - (random() * (interval '30 days')) as updated_at
FROM numbers;