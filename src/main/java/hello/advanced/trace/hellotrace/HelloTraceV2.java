package hello.advanced.trace.hellotrace;


import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HelloTraceV1의 경우 연결성에 문제가 있음, begin() 메서드 내부에서 TraceId를 생성하기 때문에 그 이전에 호출한 객체와 다른 객체를 생성해서 처리하는 방식임
 * 그로인해 id값이 동일하지 않으며 level도 업데이트되지 못하는 문제 발생
 *
 * 이를 해결하기 위해 beginSync()에서느 이전에 생성된 TraceId를 파라미터로 받아서 내부에서 createNextId()를 호출하는 방식을 통해
 * id값을 유지하면서 level을 업데이트해주는 방식으로 문제 해결
 *
 */
@Slf4j
@Component
public class HelloTraceV2 {

    private static final String START_PREFIX = "-->"; // 요청
    private static final String COMPLETE_PREFIX = "<--"; // 응답
    private static final String EX_PREFIX = "<X-"; // 예외


    public TraceStatus begin(String message) {
        TraceId traceId = new TraceId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}", traceId.getId(), addSpace(START_PREFIX,
                traceId.getLevel()), message);

        return new TraceStatus(traceId, startTimeMs, message);
    }


    // 이전 위치에서 생성한 TraceId를 파라미터로 넘겨받음
    public TraceStatus beginSync(TraceId previousTraceId, String message) {
        // 바뀐 부분 -> id 유지, level + 1
        TraceId nextId = previousTraceId.createNextId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}", nextId.getId(), addSpace(START_PREFIX,
                nextId.getLevel()), message);

        return new TraceStatus(nextId, startTimeMs, message);
    }
    public void end(TraceStatus status) {
        complete(status, null);
    }
    public void exception(TraceStatus status, Exception e) {
        complete(status, e);
    }
    private void complete(TraceStatus status, Exception e) {
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        TraceId traceId = status.getTraceId();

        // 정상호출
        if (e == null) {
            log.info("[{}] {}{} time={}ms", traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()), status.getMessage(),
                    resultTimeMs);
        }
        // 예외발생
        else {
            log.info("[{}] {}{} time={}ms ex={}", traceId.getId(),
                    addSpace(EX_PREFIX, traceId.getLevel()), status.getMessage(), resultTimeMs,
                    e.toString());
        }
    }

    // 레벨(깊이)만큼 갭을 주어서 화살표를 그림
    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append( (i == level - 1) ? "|" + prefix : "|   ");
        }
        return sb.toString();
    }

}
