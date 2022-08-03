package actions;

import java.io.IOException;
import java.util.List; //追記

import javax.servlet.ServletException;

import actions.views.EmployeeView; //追記
import actions.views.ReportView; //追記
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;  //追記
import services.EmployeeService;
import services.ReportService;  //追記

/**
 * トップページに関する処理を行うActionクラス
 *
 */
public class TopAction extends ActionBase {

    private ReportService service; //追記
    private EmployeeService emp_service;

    /**
     * indexメソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException {

        service = new ReportService(); //追記
        emp_service = new EmployeeService();

        //メソッドを実行
        invoke();

        service.close(); //追記
        emp_service.close(); //追記

    }

    /**
     * 一覧画面を表示する
     */
    public void index() throws ServletException, IOException {

        // 以下追記

        //セッションからログイン中の従業員情報を取得
        EmployeeView loginEmployee = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //ログイン中の従業員が作成した日報データを、指定されたページ数の一覧画面に表示する分取得する
        int page = getPage();
        List<ReportView> reports = service.getMinePerPage(loginEmployee, page);

        //ログイン中の従業員が作成した日報データの件数を取得
        long myReportsCount = service.countAllMine(loginEmployee);

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, myReportsCount); //ログイン中の従業員が作成した日報の数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //↑ここまで追記

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //出勤状況
        //出勤
        if(loginEmployee.getAttendanceStatus() == 0) {
            putSessionScope(AttributeConst.ATTANENCE, "出勤");
        }else if(loginEmployee.getAttendanceStatus() == 1) { //出勤中
            putSessionScope(AttributeConst.ATTANENCE, "出勤中");
        }else {
            putSessionScope(AttributeConst.ATTANENCE, "退勤");
        }

        //一覧画面を表示
        forward(ForwardConst.FW_TOP_INDEX);
    }

    /**
     * 出勤状況を更新する
     * @throws ServletException
     * @throws IOException
     */
    public void attendanceStatus() throws ServletException, IOException {

        //セッションスコープからログインしている社員オブジェクトを取得 model型のままでいい
        EmployeeView ev = (EmployeeView)getSessionScope(AttributeConst.LOGIN_EMP);

        ev.setAttendanceStatus(Integer.parseInt(getRequestParam(AttributeConst.REP_ID)));

        //従業員情報更新
        emp_service.update(ev);

        //一覧画面を表示
        redirect(ForwardConst.ACT_TOP, ForwardConst.CMD_INDEX);
    }

}
