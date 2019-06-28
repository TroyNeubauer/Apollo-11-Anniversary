package com.troy.apollo11anni;

import static com.troy.apollo11anni.Constants.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;

import org.joda.time.*;

public class Window extends JPanel {

	private JFrame frame;
	private Apollo11 apollo;

	private JTable table;
	private JScrollPane scrollPane;
	private JLabel launchTime, eventsComplete;
	private int lastEventCompleteCount = -1;
	private boolean scrolling = false;

	public Window(Apollo11 apollo) {
		super(new BorderLayout());
		this.apollo = apollo;

		frame = new JFrame();

		frame.setSize(1100, 1000);
		frame.setLocationRelativeTo(null);

		addComponents();

		frame.add(this);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void update() {
		SwingUtilities.invokeLater(() -> {
			scrolling = false;
			table.tableChanged(new TableModelEvent(apollo, 0, apollo.getRowCount(), TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
			launchTime.setText(STANDARD_FORMATTER.print(apollo.getLaunchTime()));
			eventsComplete.setText("" + apollo.getEventsComplete());

			if (apollo.getEventsComplete() != lastEventCompleteCount) {
				updateDoneEvents(apollo.getEventsComplete());
				lastEventCompleteCount = apollo.getEventsComplete();
			}
		});
	}

	private void addComponents() {
		JPanel top = new JPanel();
		top.add(new JLabel("Launch Time: "));
		top.add(launchTime = new JLabel(STANDARD_FORMATTER.print(apollo.getLaunchTime())));

		top.add(new JLabel("Events Complete: "));
		int complete = apollo.getEventsComplete();
		top.add(eventsComplete = new JLabel("" + complete));

		JPanel bottom = new JPanel();
		JButton launchNow = new JButton("Launch Now");
		launchNow.addActionListener((e) -> {
			apollo.setLaunchTime(DateTime.now().plusSeconds(30));
		});
		bottom.add(launchNow);

		JButton launchInOneDay = new JButton("Launch In One Day");
		launchInOneDay.addActionListener((e) -> {
			DateTime now = DateTime.now();
			DateTime localApollo11LaunchTime = APOLLO_11_LAUNCH_TIME.withZone(DateTimeZone.getDefault());
			if (now.getHourOfDay() >= localApollo11LaunchTime.getHourOfDay()) {// If close to launch ot after launch
				now = now.plusDays(1);// Launch tomorrow
			}
			apollo.setLaunchTime(new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), localApollo11LaunchTime.getHourOfDay(), localApollo11LaunchTime.getMinuteOfHour(),
					localApollo11LaunchTime.getSecondOfMinute()));
		});
		bottom.add(launchInOneDay);
		JButton launchOnAnni = new JButton("Launch On Anniversary");
		launchOnAnni.addActionListener((e) -> {
			apollo.setLaunchTimeToAnni();
		});
		bottom.add(launchOnAnni);

		table = new JTable(apollo, apollo.getColumnModel());

		CellRenderer renderer = new CellRenderer();
		for (int i = 0; i < apollo.getColumnModel().getColumnCount(); i++)
			apollo.getColumnModel().getColumn(i).setCellRenderer(renderer);
		scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(scrollPane, BorderLayout.CENTER);

		this.add(top, BorderLayout.NORTH);
		this.add(bottom, BorderLayout.SOUTH);

		updateDoneEvents(apollo.getEventsComplete());
	}

	private void updateDoneEvents(int complete) {
		int events = apollo.getEvents().size();
		double eventsPerDot = (scrollPane.getVerticalScrollBar().getMaximum() - scrollPane.getVerticalScrollBar().getMinimum()) / events;
		int scroll = (int) ((complete - 10) * eventsPerDot);
		scrollPane.getVerticalScrollBar().setValue(scroll);
		scrolling = true;
	}

	public class CellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setText("" + value);
			MissionEvent event = apollo.getEvents().get(row);
			Color alternate = UIManager.getColor("Table.alternateRowColor");
			if(scrolling)
				label.setBackground(Color.BLACK);
			else if (isSelected) {
				label.setBackground(table.getSelectionBackground());
			} else if (event.getTime(apollo.getLaunchTime()).isBeforeNow()) {
				label.setBackground(new Color(200, 200, 200));
				Color c = label.getForeground();
				label.setForeground(new Color(c.getRed(), c.getBlue(), c.getBlue(), 100));
			} else if (row % 2 == 1) {
				label.setBackground(new Color(250, 250, 250));
			} else {
				label.setBackground(Color.WHITE);
			}
			return label;
		}
	}
}
